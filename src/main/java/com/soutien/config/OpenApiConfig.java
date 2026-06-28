package com.soutien.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation OpenAPI / Swagger.
 *
 * Déclare le schéma de sécurité "bearer JWT" pour que l'interface
 * Swagger UI affiche un bouton "Authorize" : on y colle le token
 * obtenu via /api/auth/login, et toutes les requêtes de test
 * l'envoient automatiquement dans le header Authorization.
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI soutienOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Plateforme de Soutien Scolaire")
                        .version("1.0.0")
                        .description("API de gestion de soutien scolaire (élèves, enseignants, "
                                + "administrateurs) avec demandes d'accompagnement et messagerie. "
                                + "Authentification par JWT : appelez /api/auth/login, puis cliquez "
                                + "sur 'Authorize' et collez le token."))
                // Applique l'exigence de sécurité globalement (icône cadenas)
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SCHEME_NAME,
                        new SecurityScheme()
                                .name(SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
