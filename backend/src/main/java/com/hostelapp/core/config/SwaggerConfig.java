package com.hostelapp.core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "PG Hostel Management API",
                description = "Multi-tenant SaaS API for managing PG / Hostel operations — rooms, leases, invoices, payments, and WhatsApp AI queries.",
                version = "v1.0",
                contact = @Contact(name = "Hostel App Team")
        ),
        servers = {
                @Server(url = "/api", description = "Default server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Provide the JWT access token obtained from POST /auth/login"
)
public class SwaggerConfig {
    // Configuration is fully declarative via annotations
}
