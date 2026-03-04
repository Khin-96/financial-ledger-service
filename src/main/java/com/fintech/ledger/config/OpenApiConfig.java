package com.fintech.ledger.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Financial Ledger Service API",
        version = "1.0.0",
        description = "Event-sourced payment ledger with CQRS and double-entry accounting",
        contact = @Contact(
            name = "Chris Kinga Hinzano",
            email = "hinzanno@gmail.com",
            url = "https://hinzano.dev"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8081", description = "Local server")
    }
)
public class OpenApiConfig {
}
