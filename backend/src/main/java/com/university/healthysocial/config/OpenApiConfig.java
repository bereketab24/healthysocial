package com.university.healthysocial.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String OAUTH2_SCHEME = "keycloak_oauth2";

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${springdoc.swagger-ui.oauth.issuer-uri:http://localhost:8180/realms/healthy-social}")
    private String swaggerIssuerUri;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(OAUTH2_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(OAUTH2_SCHEME, keycloakSecurityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Healthy Lifestyle Social API")
                .description("""
                        REST API for a social platform that promotes healthy living.
                        
                        Features: habit tracking, goal setting, motivational posts,
                        social feeds, community challenges, and leaderboards.
                        
                        Authentication is handled by Keycloak. Obtain a Bearer token
                        via the OAuth2 Authorization Code flow below, then click
                        **Authorize** to use protected endpoints.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Group 2")
                        .email("bereketabhaileeyesus.shanka@student.wsb.edu.pl Or mustafa.khairee@student.wsb.edu.pl"))
                .license(new License().name("MIT"));
    }

    private SecurityScheme keycloakSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Keycloak OAuth2 Authorization Code Flow")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl(swaggerIssuerUri + "/protocol/openid-connect/auth")
                                .tokenUrl(swaggerIssuerUri + "/protocol/openid-connect/token")
                                .refreshUrl(swaggerIssuerUri + "/protocol/openid-connect/token")));
    }
}