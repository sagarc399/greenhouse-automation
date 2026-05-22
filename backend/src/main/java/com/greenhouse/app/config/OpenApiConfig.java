package com.greenhouse.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI configuration for the Greenhouse Management System REST API.
 *
 * <p>Swagger UI is available at {@code /swagger-ui.html}.
 * API docs JSON is available at {@code /api-docs}.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Defines the OpenAPI specification with metadata and OAuth2 security scheme.
     *
     * @return the configured {@link OpenAPI} bean
     */
    @Bean
    public OpenAPI greenhouseOpenAPI() {
        final String securitySchemeName = "oauth2";

        return new OpenAPI()
            .info(new Info()
                .title("Greenhouse Management System API")
                .description("REST API for managing greenhouses, zones, sensors, actuators, automation rules, and alerts.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Greenhouse Team")
                    .email("admin@greenhouse.app"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.OAUTH2)
                    .description("OAuth2 via Google")
                    .flows(new io.swagger.v3.oas.models.security.OAuthFlows()
                        .authorizationCode(new io.swagger.v3.oas.models.security.OAuthFlow()
                            .authorizationUrl("/oauth2/authorization/google")
                            .tokenUrl("/login/oauth2/code/google")))));
    }
}
