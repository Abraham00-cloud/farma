package com.project.farma.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Project Farma API Engine")
                        .version("1.0")
                        .description("Automated Multi-Tenant Farm Management Core Architecture System APIs"))
                // 1. Force Swagger to recognize that a JWT Authorization header is required
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // 2. Define the exact input format for the global Authorize padlock dialog box
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Provide your secure multi-tenant JWT token access passport.")));
    }
}