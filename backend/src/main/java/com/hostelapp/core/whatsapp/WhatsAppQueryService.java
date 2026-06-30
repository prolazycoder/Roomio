package com.hostelapp.core.whatsapp;

import com.hostelapp.core.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Orchestrates the full WhatsApp → AI → SQL → reply pipeline.
 *
 * Flow:
 * 1. Extract message text and sender phone number from the webhook payload
 * 2. Resolve the sender's workspace (by matching phone number to a user or using a default)
 * 3. Call AiQueryTranslator to get a SQL SELECT from the natural-language question
 * 4. Call SafeQueryExecutor to validate + run the query
 * 5. Format the result and send via WhatsAppSender
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppQueryService {

    private final AiQueryTranslator aiQueryTranslator;
    private final SafeQueryExecutor safeQueryExecutor;
    private final ResultFormatter resultFormatter;
    private final WhatsAppSender whatsAppSender;
    private final WorkspaceRepository workspaceRepository;

    /**
     * Handles an incoming WhatsApp text message asynchronously.
     * Running async ensures the webhook POST returns 200 immediately,
     * satisfying Meta's 20-second acknowledgement requirement.
     *
     * @param payload the parsed webhook payload
     */
    @Async
    public void handleIncoming(WhatsAppWebhookPayload payload) {
        try {
            if (payload.getEntry() == null || payload.getEntry().isEmpty()) return;

            for (WhatsAppWebhookPayload.Entry entry : payload.getEntry()) {
                if (entry.getChanges() == null) continue;

                for (WhatsAppWebhookPayload.Change change : entry.getChanges()) {
                    WhatsAppWebhookPayload.Value value = change.getValue();
                    if (value == null || value.getMessages() == null) continue;

                    for (WhatsAppWebhookPayload.Message message : value.getMessages()) {
                        processMessage(message);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error handling WhatsApp webhook payload: {}", e.getMessage(), e);
        }
    }

    private void processMessage(WhatsAppWebhookPayload.Message message) {
        if (!"text".equals(message.getType()) || message.getText() == null) {
            log.debug("Ignoring non-text message from {}", message.getFrom());
            return;
        }

        String from = message.getFrom();
        String question = message.getText().getBody();
        String messageId = message.getId();

        log.info("Received WhatsApp query from {}: {}", from, question);

        // Mark message as read to show typing indicator
        whatsAppSender.markAsRead(messageId);

        // Resolve the workspace for this sender.
        // Strategy: use workspace ID = 1 as fallback for now.
        // In production, map the sender's phone to a user in the DB.
        Long workspaceId = resolveWorkspace(from);

        if (workspaceId == null) {
            whatsAppSender.sendText(from,
                    "⚠️ I couldn't find a workspace linked to your phone number. " +
                    "Please contact your hostel administrator.");
            return;
        }

        // Translate question to SQL
        String sql;
        try {
            sql = aiQueryTranslator.translate(question, workspaceId);
        } catch (Exception e) {
            log.error("AI translation failed: {}", e.getMessage());
            whatsAppSender.sendText(from, resultFormatter.formatError("AI service unavailable. Please try again later."));
            return;
        }

        if ("CANNOT_ANSWER".equals(sql) || sql == null || sql.isBlank()) {
            whatsAppSender.sendText(from, resultFormatter.formatError(
                    "I couldn't convert your question into a database query. " +
                    "Please rephrase or ask about rooms, leases, invoices, or payments."));
            return;
        }

        log.info("AI generated SQL for workspace {}: {}", workspaceId, sql);

        // Execute the query
        List<Map<String, Object>> rows;
        try {
            rows = safeQueryExecutor.execute(sql);
        } catch (IllegalArgumentException e) {
            log.warn("Query blocked by safety check: {}", e.getMessage());
            whatsAppSender.sendText(from, resultFormatter.formatError("Security validation failed: " + e.getMessage()));
            return;
        } catch (Exception e) {
            log.error("Query execution failed: {}", e.getMessage());
            whatsAppSender.sendText(from, resultFormatter.formatError("Query execution failed. Please try a simpler question."));
            return;
        }

        // Format and send the result
        String reply = resultFormatter.format(rows);
        whatsAppSender.sendText(from, reply);
    }

    /**
     * Resolves the workspace ID for a given phone number.
     * Currently returns the first available workspace as a default.
     * TODO: In production, look up the user by phone number.
     *
     * @param phoneNumber the E.164 phone number string
     * @return the workspace ID, or null if not found
     */
    private Long resolveWorkspace(String phoneNumber) {
        return workspaceRepository.findAll()
                .stream()
                .findFirst()
                .map(w -> w.getId())
                .orElse(null);
    }
}
