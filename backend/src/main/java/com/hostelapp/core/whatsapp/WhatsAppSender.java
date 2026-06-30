package com.hostelapp.core.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Sends WhatsApp messages via the Meta Cloud API.
 *
 * Reference: https://developers.facebook.com/docs/whatsapp/cloud-api/messages/text-messages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppSender {

    private final WhatsAppProperties whatsAppProperties;
    private final WebClient.Builder webClientBuilder;

    /**
     * Sends a text message to a WhatsApp number.
     *
     * @param to      the recipient phone number in E.164 format (e.g. 919876543210)
     * @param message the text body to send
     */
    public void sendText(String to, String message) {
        String url = whatsAppProperties.getApiUrl()
                + "/" + whatsAppProperties.getPhoneNumberId()
                + "/messages";

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", to,
                "type", "text",
                "text", Map.of(
                        "preview_url", false,
                        "body", message
                )
        );

        try {
            webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header("Authorization", "Bearer " + whatsAppProperties.getAccessToken())
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .subscribe(
                            response -> log.info("WhatsApp message sent to {}: {}", to, response),
                            error -> log.error("Failed to send WhatsApp message to {}: {}", to, error.getMessage())
                    );
        } catch (Exception e) {
            log.error("Exception while sending WhatsApp message: {}", e.getMessage(), e);
        }
    }

    /**
     * Sends a typing indicator (mark as read + show typing).
     *
     * @param messageId the incoming message ID to mark as read
     */
    public void markAsRead(String messageId) {
        String url = whatsAppProperties.getApiUrl()
                + "/" + whatsAppProperties.getPhoneNumberId()
                + "/messages";

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "status", "read",
                "message_id", messageId
        );

        try {
            webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header("Authorization", "Bearer " + whatsAppProperties.getAccessToken())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .subscribe(
                            r -> log.debug("Marked message {} as read", messageId),
                            e -> log.warn("Could not mark message as read: {}", e.getMessage())
                    );
        } catch (Exception e) {
            log.warn("Exception marking message as read: {}", e.getMessage());
        }
    }
}
