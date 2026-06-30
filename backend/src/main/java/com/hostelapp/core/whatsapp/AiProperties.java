package com.hostelapp.core.whatsapp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /** Gemini API key */
    private String geminiApiKey;

    /** Gemini model name, e.g. gemini-1.5-flash */
    private String geminiModel;

    /** Gemini base URL */
    private String geminiApiUrl;

    /** Maximum rows to return from a query result */
    private int maxResultRows = 20;
}
