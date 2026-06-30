package com.hostelapp.core.whatsapp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.whatsapp")
public class WhatsAppProperties {

    /** Token used to verify the webhook with Meta */
    private String verifyToken;

    /** Meta phone number ID (from WhatsApp Business Account) */
    private String phoneNumberId;

    /** Meta permanent access token */
    private String accessToken;

    /** Base URL for Graph API, e.g. https://graph.facebook.com/v19.0 */
    private String apiUrl;
}
