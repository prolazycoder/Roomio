package com.hostelapp.core;

import com.hostelapp.core.whatsapp.AiProperties;
import com.hostelapp.core.whatsapp.WhatsAppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({WhatsAppProperties.class, AiProperties.class})
public class HostelAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(HostelAppApplication.class, args);
    }
}
