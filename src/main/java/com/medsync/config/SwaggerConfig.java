package com.medsync.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "MedSync API",
        version = "1.0",
        description = "Smart Hospital Resource & Appointment Management System"
    )
)
// Tells Swagger: "there is a security scheme called 'bearerAuth'.
// It's a JWT token sent in the Authorization header as: Bearer <token>"
// Controllers reference this scheme with @SecurityRequirement(name = "bearerAuth")
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
    // No bean methods needed — the annotations above do all the work.
    // springdoc-openapi scans them at startup and generates the UI config.
}