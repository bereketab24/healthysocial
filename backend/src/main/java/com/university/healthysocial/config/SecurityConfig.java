package com.university.healthysocial.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless JWT-based security configuration.
 *
 * <ul>
 *   <li>All requests require a valid Keycloak JWT – except a small set of public read-only endpoints.</li>
 *   <li>Sessions are never created on the server side (STATELESS).</li>
 *   <li>CSRF is disabled because the API is consumed by non-browser clients that present a Bearer token.</li>
 *   <li>Method-level security ({@code @PreAuthorize}) is enabled via {@link EnableMethodSecurity}.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakJwtAuthConverter jwtAuthConverter;

    /** Publicly accessible paths (no token required). */
    private static final String[] PUBLIC_GET_PATTERNS = {
            "/api/posts",
            "/api/posts/{id}",
            "/api/users/{id}/profile",
            "/api/leaderboard",
            "/api/challenges",
            // Infrastructure & docs
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATTERNS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );

        return http.build();
    }
}