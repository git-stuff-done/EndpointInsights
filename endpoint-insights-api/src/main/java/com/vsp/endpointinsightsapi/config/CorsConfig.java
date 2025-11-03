package com.vsp.endpointinsightsapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.cors")
@Data
public class CorsConfig implements WebMvcConfigurer {

    private List<String> allowedOrigins = List.of("http://localhost:8080");
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
    private boolean allowCredentials = true;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods(allowedMethods.toArray(String[]::new))
                .allowedHeaders("*")
                .allowCredentials(allowCredentials);
    }
}
