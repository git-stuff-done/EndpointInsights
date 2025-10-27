package com.vsp.endpointinsightsapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for authentication settings.
 * Loads values from application.yml under the 'app.authentication' prefix.
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

    @Data
    public static class Groups {
        private String read = "ei:read";
        private String write = "ei:write";
    }

    @Data
    public static class Claims {
        private String username = "preferred_username";
        private String email = "email";
        private String groups = "groups";
    }

    @Data
    public static class Endpoints {
        private List<String> PublicEndpoints = List.of("/api/health");
    }
}