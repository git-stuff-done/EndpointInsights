package com.vsp.endpointinsightsapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for authentication and authorization settings.
 *
 * <p>Loads values from application.yaml under the {@code app.authentication} prefix.
 * Provides configuration for JWT claim names, role group mappings, and public endpoints.
 *
 * <h2>Example Configuration:</h2>
 * <pre>{@code
 * app:
 *   authentication:
 *     groups:
 *       read: "ei:read"
 *       write: "ei:write"
 *     claims:
 *       username: "preferred_username"
 *       email: "email"
 *       groups: "groups"
 *     endpoints:
 *       public-endpoints:
 *         - "/api/health"
 * }</pre>
 *
 * @see com.vsp.endpointinsightsapi.authentication.AuthorizationInterceptor
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.authentication")
public class AuthenticationProperties {

    @NestedConfigurationProperty
    private Groups groups = new Groups();

    @NestedConfigurationProperty
    private Claims claims = new Claims();

    @NestedConfigurationProperty
    private Endpoints endpoints = new Endpoints();

    /**
     * Group name mappings for role-based access control.
     */
    @Data
    public static class Groups {
        /** Group name for read access */
        private String read = "ei:read";
        /** Group name for write access (includes read) */
        private String write = "ei:write";
    }

    /**
     * JWT claim name mappings.
     */
    @Data
    public static class Claims {
        /** Claim name for username (default: preferred_username) */
        private String username = "preferred_username";
        /** Claim name for email (default: email) */
        private String email = "email";
        /** Claim name for groups list (default: groups) */
        private String groups = "groups";
    }

    /**
     * Public endpoint configuration.
     */
    @Data
    public static class Endpoints {
        /** List of endpoints that bypass authentication */
        private List<String> publicEndpoints = List.of("/api/health");
    }
}