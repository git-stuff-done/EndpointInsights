package com.vsp.endpointinsightsapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    @Test
    void shouldCreateOpenAPIBean() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        assertNotNull(openAPI);
    }

    @Test
    void shouldHaveCorrectAPITitle() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        assertEquals("Endpoint Insights API", openAPI.getInfo().getTitle());
    }

    @Test
    void shouldHaveCorrectAPIVersion() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void shouldHaveCorrectAPIDescription() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        String expectedDescription = "REST API documentation for Endpoint Insights - A comprehensive endpoint testing and monitoring solution";
        
        assertEquals(expectedDescription, openAPI.getInfo().getDescription());
    }

    @Test
    void shouldHaveContactInformation() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        assertNotNull(openAPI.getInfo().getContact());
        assertEquals("VSP Development Team", openAPI.getInfo().getContact().getName());
        assertEquals("https://vsp.com", openAPI.getInfo().getContact().getUrl());
        assertEquals("support@vsp.com", openAPI.getInfo().getContact().getEmail());
    }

    @Test
    void shouldHaveLicenseInformation() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        assertNotNull(openAPI.getInfo().getLicense());
        assertEquals("MIT License", openAPI.getInfo().getLicense().getName());
    }

    @Test
    void shouldHaveOAuth2SecurityScheme() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("oauth2"));
    }

    @Test
    void shouldConfigureOAuth2Correctly() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        SecurityScheme oauth2 = openAPI.getComponents().getSecuritySchemes().get("oauth2");
        
        assertEquals(SecurityScheme.Type.OAUTH2, oauth2.getType());
        assertEquals("bearer", oauth2.getScheme());
        assertEquals("JWT", oauth2.getBearerFormat());
        assertEquals("OAuth2 authentication using JWT Bearer tokens", oauth2.getDescription());
    }

    @Test
    void shouldHaveSecurityRequirement() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
        assertTrue(openAPI.getSecurity().get(0).containsKey("oauth2"));
    }

    @Test
    void shouldReturnSameInstanceOnMultipleCalls() {
        OpenAPI openAPI1 = openApiConfig.customOpenAPI();
        OpenAPI openAPI2 = openApiConfig.customOpenAPI();
        
        assertNotNull(openAPI1);
        assertNotNull(openAPI2);
        // Both should have the same configuration even if different instances
        assertEquals(openAPI1.getInfo().getTitle(), openAPI2.getInfo().getTitle());
    }
}
