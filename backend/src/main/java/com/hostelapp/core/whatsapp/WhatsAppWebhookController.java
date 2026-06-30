package com.hostelapp.core.whatsapp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook controller for Meta WhatsApp Cloud API.
 *
 * GET  /api/whatsapp/webhook  — webhook verification challenge (called by Meta once during setup)
 * POST /api/whatsapp/webhook  — receives incoming messages from Meta
 *
 * Both endpoints are public (no JWT required) — permitted in SecurityConfig.
 */
@RestController
@RequestMapping("/whatsapp/webhook")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "WhatsApp AI Query", description = "Meta WhatsApp webhook for natural-language hostel data queries powered by Gemini AI")
public class WhatsAppWebhookController {

    private final WhatsAppQueryService whatsAppQueryService;
    private final WhatsAppProperties whatsAppProperties;

    /**
     * Meta calls this GET endpoint to verify the webhook during setup.
     * It checks the hub.verify_token, and echoes back hub.challenge on success.
     */
    @GetMapping
    @Operation(
            summary = "Verify webhook (Meta handshake)",
            description = "Called by Meta to verify the webhook URL. Responds with the hub.challenge value if the verify token matches."
    )
    @ApiResponse(responseCode = "200", description = "Verification successful")
    @ApiResponse(responseCode = "403", description = "Token mismatch — verification failed")
    public ResponseEntity<String> verifyWebhook(
            @Parameter(description = "Should be 'subscribe'") @RequestParam("hub.mode") String mode,
            @Parameter(description = "Token Meta sends to verify") @RequestParam("hub.verify_token") String token,
            @Parameter(description = "Challenge value to echo back") @RequestParam("hub.challenge") String challenge
    ) {
        log.info("WhatsApp webhook verification — mode: {}, token: {}", mode, token);

        if ("subscribe".equals(mode) && whatsAppProperties.getVerifyToken().equals(token)) {
            log.info("WhatsApp webhook verified successfully.");
            return ResponseEntity.ok(challenge);
        }

        log.warn("WhatsApp webhook verification FAILED — token mismatch.");
        return ResponseEntity.status(403).body("Verification failed");
    }

    /**
     * Meta sends all incoming WhatsApp messages to this POST endpoint.
     * We return 200 immediately and process asynchronously.
     */
    @PostMapping
    @Operation(
            summary = "Receive WhatsApp messages",
            description = "Called by Meta when a user sends a WhatsApp message. " +
                    "The message text is interpreted as a natural-language query, " +
                    "converted to SQL by Gemini AI, executed on the hostel DB, " +
                    "and the result is sent back to the user via WhatsApp."
    )
    @ApiResponse(responseCode = "200", description = "Message received (processing is asynchronous)")
    public ResponseEntity<String> receiveMessage(@RequestBody WhatsAppWebhookPayload payload) {
        log.info("Received WhatsApp webhook event: {}", payload.getObject());

        // Process asynchronously — Meta requires a 200 response within 20 seconds
        whatsAppQueryService.handleIncoming(payload);

        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
