package com.university.healthysocial.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility that provides convenient access to the authenticated principal's
 * details extracted from the Keycloak JWT.
 */
@Component
public class SecurityUtils {

    private static final String EMAIL_CLAIM = "email";
    private static final String PREFERRED_USERNAME_CLAIM = "preferred_username";

    /**
     * Returns the Keycloak {@code sub} (subject) claim of the currently
     * authenticated user, which we store as {@code keycloakId} in our DB.
     */
    public String getCurrentKeycloakId() {
        return getJwt()
                .map(Jwt::getSubject)
                .orElseThrow(() -> new IllegalStateException("No authenticated user in SecurityContext"));
    }

    public Optional<String> getCurrentEmail() {
        return getJwt().map(jwt -> jwt.getClaimAsString(EMAIL_CLAIM));
    }

    public Optional<String> getCurrentPreferredUsername() {
        return getJwt().map(jwt -> jwt.getClaimAsString(PREFERRED_USERNAME_CLAIM));
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String);
    }

    // ──────────────────────────────────────────────────────────────────────────

    private Optional<Jwt> getJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtToken) {
            return Optional.of(jwtToken.getToken());
        }
        return Optional.empty();
    }
}