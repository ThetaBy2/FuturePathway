package com.futurepathway.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {

    @Bean
    public OpenAPI futurePathwayApi() {
        return new OpenAPI()
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("Future Pathway API")
                        .version("1.0.0")
                        .description("""
                                Campus placement & career-readiness platform.
                                Roles: STUDENT · COLLEGE · COMPANY · ADMIN.
                                Authenticate via `POST /api/auth/login`, then click **Authorize** and paste the JWT.
                                AI endpoints call Google Gemini; when no API key is configured they use a \
                                clearly-labelled heuristic fallback (`"source": "heuristic-fallback"`).""")
                        .contact(new Contact().name("Future Pathway").email("admin@futurepathway.local")));
    }
}
