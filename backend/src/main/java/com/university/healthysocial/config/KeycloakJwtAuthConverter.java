package com.university.healthysocial.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts a Keycloak-issued JWT into a Spring Security {@link AbstractAuthenticationToken}.
 *
 * <p>Keycloak places realm-level roles inside {@code realm_access.roles} and
 * client-level roles inside {@code resource_access.<clientId>.roles}.
 * Both are extracted and prefixed with {@code ROLE_} so that Spring's
 * standard {@code @PreAuthorize("hasRole('ADMIN')")} checks work as expected.
 */
@Component
public class KeycloakJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    /** Fallback Spring Security scope/claim converter. */
    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    @Value("${app.keycloak.resource}")
    private String clientId;

    @Override
    @NonNull
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream
                .concat(defaultConverter.convert(jwt).stream(), extractRealmRoles(jwt).stream())
                .collect(Collectors.toUnmodifiableSet());

        // Use the 'sub' claim as the principal name so we can look up the user later
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    // ──────────────────────────────────────────────────────────────────────────

    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Set<GrantedAuthority> roles = extractFromRealmAccess(jwt);
        roles.addAll(extractFromResourceAccess(jwt));
        return roles;
    }

    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> extractFromRealmAccess(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
        if (realmAccess == null || !realmAccess.containsKey(ROLES_CLAIM)) {
            return Set.of();
        }
        List<String> roles = (List<String>) realmAccess.get(ROLES_CLAIM);
        return toGrantedAuthorities(roles);
    }

    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> extractFromResourceAccess(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap(RESOURCE_ACCESS_CLAIM);
        if (resourceAccess == null || !resourceAccess.containsKey(clientId)) {
            return Set.of();
        }
        Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
        if (clientAccess == null || !clientAccess.containsKey(ROLES_CLAIM)) {
            return Set.of();
        }
        List<String> roles = (List<String>) clientAccess.get(ROLES_CLAIM);
        return toGrantedAuthorities(roles);
    }

    private Set<GrantedAuthority> toGrantedAuthorities(List<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
                .collect(Collectors.toSet());
    }
}