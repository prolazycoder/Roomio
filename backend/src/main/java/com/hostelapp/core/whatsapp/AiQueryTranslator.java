package com.hostelapp.core.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Calls the Gemini API to translate a natural-language hostel inquiry
 * into a read-only SQL SELECT query based on the known schema.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiQueryTranslator {

    private static final String DB_SCHEMA_CONTEXT = """
            You are a SQL expert for a PostgreSQL database used by a PG/Hostel Management SaaS application.
            The database uses shared-schema multi-tenancy. Every tenant table has a `workspace_id` column.
            
            Tables and columns:
            - organizations(id, name, created_at)
            - workspaces(id, organization_id, name, subdomain, created_at)
            - users(id, workspace_id, email, password_hash, role[OWNER/MANAGER/TENANT], gov_id_type, gov_id_number_masked, created_at)
            - rooms(id, workspace_id, room_number, total_beds, vacant_beds, price_per_month)
            - leases(id, workspace_id, tenant_id→users.id, room_id→rooms.id, lease_type[MONTHLY/FIXED_PERIOD], start_date, end_date, rent_amount, status[ACTIVE/TERMINATED/COMPLETED], created_at)
            - invoices(id, workspace_id, lease_id→leases.id, amount, due_date, status[PENDING/PAID/OVERDUE], created_at)
            - invoice_items(id, invoice_id→invoices.id, description, amount)
            - payments(id, workspace_id, invoice_id→invoices.id, amount, payment_date, payment_method[UPI/CARD/CASH/NET_BANKING], transaction_reference, created_at)
            
            RULES:
            1. ALWAYS return ONLY a single SQL SELECT statement — no explanations, no markdown, no code blocks.
            2. ALWAYS include `WHERE workspace_id = {workspaceId}` for any tenant table query.
            3. NEVER write INSERT, UPDATE, DELETE, DROP, CREATE, ALTER, TRUNCATE or any DDL/DML.
            4. LIMIT results to 20 rows maximum.
            5. If the question cannot be answered with a SELECT query, return exactly: CANNOT_ANSWER
            6. Do not include password_hash or gov_id_number_masked columns.
            """;

    private final AiProperties aiProperties;
    private final WebClient.Builder webClientBuilder;

    /**
     * Translates a natural language question into a SQL SELECT query.
     *
     * @param question    the user's natural language question
     * @param workspaceId the tenant workspace ID to scope the query
     * @return a SQL SELECT string, or "CANNOT_ANSWER"
     */
    public String translate(String question, Long workspaceId) {
        String prompt = DB_SCHEMA_CONTEXT.replace("{workspaceId}", String.valueOf(workspaceId))
                + "\n\nUser question: " + question
                + "\n\nSQL query:";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.0,
                        "maxOutputTokens", 512
                )
        );

        String url = aiProperties.getGeminiApiUrl()
                + "/models/" + aiProperties.getGeminiModel()
                + ":generateContent?key=" + aiProperties.getGeminiApiKey();

        try {
            Map<?, ?> response = webClientBuilder.build()
                    .post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return extractText(response);
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            return "CANNOT_ANSWER";
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> response) {
        if (response == null) return "CANNOT_ANSWER";
        try {
            List<?> candidates = (List<?>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "CANNOT_ANSWER";

            Map<?, ?> content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
            List<?> parts = (List<?>) content.get("parts");
            String text = ((Map<?, ?>) parts.get(0)).get("text").toString().trim();

            // Strip markdown code fences if present
            if (text.startsWith("```")) {
                text = text.replaceAll("```[a-zA-Z]*\\n?", "").replaceAll("```", "").trim();
            }
            return text;
        } catch (Exception e) {
            log.warn("Failed to parse Gemini response: {}", e.getMessage());
            return "CANNOT_ANSWER";
        }
    }
}
