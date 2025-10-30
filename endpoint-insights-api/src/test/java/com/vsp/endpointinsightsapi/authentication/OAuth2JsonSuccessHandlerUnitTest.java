package com.vsp.endpointinsightsapi.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsp.endpointinsightsapi.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class OAuth2JsonSuccessHandlerUnitTest {

    @Mock
    private Authentication nonOAuth2Authentication;

    @Mock
    private OAuth2AuthenticationToken oauth2AuthenticationToken;

    @Mock
    private OAuth2User nonOidcUser;

    @Mock
    private OidcUser oidcUser;

    @Mock
    private OidcIdToken oidcIdToken;

    private OAuth2JsonSuccessHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        handler = new OAuth2JsonSuccessHandler();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldSuccessfullyHandleValidOAuth2Authentication() throws IOException {
        String username = "testuser";
        String email = "test@example.com";
        String userName = "Test User";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        String tokenValue = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9...";

        when(oauth2AuthenticationToken.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getName()).thenReturn(userName);
        when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
        when(oidcUser.getAttribute("preferred_username")).thenReturn(username);
        when(oidcUser.getAttribute("email")).thenReturn(email);
        when(oidcIdToken.getExpiresAt()).thenReturn(expiresAt);
        when(oidcIdToken.getTokenValue()).thenReturn(tokenValue);

        handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);

        assertEquals("application/json", response.getContentType());
        assertEquals(200, response.getStatus());

        String responseBody = response.getContentAsString();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains(tokenValue));
        assertTrue(responseBody.contains(username));
        assertTrue(responseBody.contains(email));
        assertTrue(responseBody.contains(String.valueOf(expiresAt.getEpochSecond())));

        verify(oidcUser).getName();
        verify(oidcUser).getIdToken();
        verify(oidcUser).getAttribute("preferred_username");
        verify(oidcUser).getAttribute("email");
        verify(oidcIdToken).getExpiresAt();
        verify(oidcIdToken).getTokenValue();
    }

    @Test
    void shouldThrowExceptionForNullPrincipal() {
        when(oauth2AuthenticationToken.getPrincipal()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });
    }

    @Test
    void shouldThrowExceptionForNonOAuth2AuthenticationToken() {
        CustomException exception = assertThrows(CustomException.class, () -> {
            handler.onAuthenticationSuccess(request, response, nonOAuth2Authentication);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void shouldThrowExceptionForNonOidcUserPrincipal() {
        when(oauth2AuthenticationToken.getPrincipal()).thenReturn(nonOidcUser);

        CustomException exception = assertThrows(CustomException.class, () -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

        verify(oauth2AuthenticationToken, times(2)).getPrincipal();
    }

    @Test
    void shouldThrowExceptionForMissingIdToken() {
        when(oauth2AuthenticationToken.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getName()).thenReturn("Test User");
        when(oidcUser.getIdToken()).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(oidcUser, times(2)).getName();
        verify(oidcUser).getIdToken();
    }

    @Test
    void shouldThrowExceptionForMissingUsername() {
        when(oauth2AuthenticationToken.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getName()).thenReturn("Test User");
        when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
        when(oidcUser.getAttribute("preferred_username")).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(oidcUser).getAttribute("preferred_username");
    }

    @Test
    void shouldThrowExceptionForMissingEmail() {
        when(oauth2AuthenticationToken.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getName()).thenReturn("Test User");
        when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
        when(oidcUser.getAttribute("preferred_username")).thenReturn("testuser");
        when(oidcUser.getAttribute("email")).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(oidcUser).getAttribute("email");
    }

    @Test
    void shouldThrowExceptionForMissingExpiresAt() {
        when(oauth2AuthenticationToken.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getName()).thenReturn("Test User");
        when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
        when(oidcUser.getAttribute("preferred_username")).thenReturn("testuser");
        when(oidcUser.getAttribute("email")).thenReturn("test@example.com");
        when(oidcIdToken.getExpiresAt()).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(oidcIdToken).getExpiresAt();
    }

    @Test
    void shouldHandleEmptyStringUsername() {
        when(oauth2AuthenticationToken.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getName()).thenReturn("Test User");
        when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
        when(oidcUser.getAttribute("preferred_username")).thenReturn("");

        CustomException exception = assertThrows(CustomException.class, () -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void shouldHandleEmptyStringEmail() {
        when(oauth2AuthenticationToken.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getName()).thenReturn("Test User");
        when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
        when(oidcUser.getAttribute("preferred_username")).thenReturn("testuser");
        when(oidcUser.getAttribute("email")).thenReturn("");

        CustomException exception = assertThrows(CustomException.class, () -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void shouldHandleRealOidcUserScenario() throws IOException {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(3600);

        Map<String, Object> claims = Map.of(
                "sub", "user123",
                "preferred_username", "realuser",
                "email", "realuser@example.com",
                "name", "Real User",
                "iat", now.getEpochSecond(),
                "exp", expiresAt.getEpochSecond()
        );

        OidcIdToken realIdToken = new OidcIdToken(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.real.token",
                now,
                expiresAt,
                claims
        );

        Map<String, Object> attributes = Map.of(
                "sub", "user123",
                "preferred_username", "realuser",
                "email", "realuser@example.com",
                "name", "Real User"
        );

        OidcUser realOidcUser = new DefaultOidcUser(
                Collections.emptyList(),
                realIdToken
        );

        OAuth2AuthenticationToken realOAuth2Token = new OAuth2AuthenticationToken(
                realOidcUser,
                Collections.emptyList(),
                "oidc-client"
        );

        handler.onAuthenticationSuccess(request, response, realOAuth2Token);

        assertEquals("application/json", response.getContentType());
        assertEquals(200, response.getStatus());

        String responseBody = response.getContentAsString();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("realuser"));
        assertTrue(responseBody.contains("realuser@example.com"));
        assertTrue(responseBody.contains("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.real.token"));
    }

    @Test
    void shouldCreateValidJsonResponse() throws IOException {
        String username = "jsonuser";
        String email = "json@example.com";
        Instant expiresAt = Instant.ofEpochSecond(1234567890);
        String tokenValue = "token.with.dots";

        when(oauth2AuthenticationToken.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getName()).thenReturn("JSON User");
        when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
        when(oidcUser.getAttribute("preferred_username")).thenReturn(username);
        when(oidcUser.getAttribute("email")).thenReturn(email);
        when(oidcIdToken.getExpiresAt()).thenReturn(expiresAt);
        when(oidcIdToken.getTokenValue()).thenReturn(tokenValue);

        handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);

        String responseBody = response.getContentAsString();

        Map<String, Object> parsedResponse = objectMapper.readValue(responseBody, Map.class);

        assertEquals(tokenValue, parsedResponse.get("idToken"));
        assertEquals(1234567890, parsedResponse.get("expiresAt"));
        assertEquals(username, parsedResponse.get("username"));
        assertEquals(email, parsedResponse.get("email"));
        assertEquals(4, parsedResponse.size());
    }

    @Test
    void shouldHandleSpecialCharactersInAttributes() throws IOException {
        String username = "user@domain.com";
        String email = "test+tag@example.com";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        String tokenValue = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.special.chars";

        when(oauth2AuthenticationToken.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getName()).thenReturn("Special Char User");
        when(oidcUser.getIdToken()).thenReturn(oidcIdToken);
        when(oidcUser.getAttribute("preferred_username")).thenReturn(username);
        when(oidcUser.getAttribute("email")).thenReturn(email);
        when(oidcIdToken.getExpiresAt()).thenReturn(expiresAt);
        when(oidcIdToken.getTokenValue()).thenReturn(tokenValue);

        handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);

        assertEquals(200, response.getStatus());
        String responseBody = response.getContentAsString();
        assertTrue(responseBody.contains(username));
        assertTrue(responseBody.contains(email));
    }
}