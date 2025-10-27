package com.vsp.endpointinsightsapi.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class JWTAuthenticationSuccessHandlerUnitTest {

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @Mock
    private OAuth2AuthorizedClient authorizedClient;

    @Mock
    private OAuth2AuthenticationToken oauth2AuthenticationToken;

    @Mock
    private Authentication nonOAuth2Authentication;

    private JWTAuthenticationSuccessHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new JWTAuthenticationSuccessHandler(authorizedClientService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldHandleSuccessfulOAuth2Authentication() throws IOException {
        String clientRegistrationId = "oidc-client";
        String principalName = "testuser@example.com";
        String authorizedClientPrincipalName = "testuser@example.com";

        when(oauth2AuthenticationToken.getName()).thenReturn(principalName);
        when(oauth2AuthenticationToken.getAuthorizedClientRegistrationId()).thenReturn(clientRegistrationId);

        when(authorizedClientService.loadAuthorizedClient(
                eq(clientRegistrationId),
                eq(principalName)
        )).thenReturn(authorizedClient);

        when(authorizedClient.getPrincipalName()).thenReturn(authorizedClientPrincipalName);

        assertDoesNotThrow(() -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        verify(oauth2AuthenticationToken, times(2)).getName();
        verify(oauth2AuthenticationToken).getAuthorizedClientRegistrationId();
        verify(authorizedClientService).loadAuthorizedClient(clientRegistrationId, principalName);
        verify(authorizedClient).getPrincipalName();
    }

    @Test
    void shouldHandleNullAuthorizedClient() throws IOException {
        String clientRegistrationId = "oidc-client";
        String principalName = "testuser@example.com";

        when(oauth2AuthenticationToken.getName()).thenReturn(principalName);
        when(oauth2AuthenticationToken.getAuthorizedClientRegistrationId()).thenReturn(clientRegistrationId);

        when(authorizedClientService.loadAuthorizedClient(
                eq(clientRegistrationId),
                eq(principalName)
        )).thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        verify(authorizedClientService).loadAuthorizedClient(clientRegistrationId, principalName);
    }

    @Test
    void shouldHandleClassCastExceptionForNonOAuth2Authentication() {
        when(nonOAuth2Authentication.getName()).thenReturn("testuser");

        assertThrows(ClassCastException.class, () -> {
            handler.onAuthenticationSuccess(request, response, nonOAuth2Authentication);
        });

        verifyNoInteractions(authorizedClientService);
    }

    @Test
    void shouldHandleEmptyPrincipalName() throws IOException {
        String clientRegistrationId = "oidc-client";
        String principalName = "";

        when(oauth2AuthenticationToken.getName()).thenReturn(principalName);
        when(oauth2AuthenticationToken.getAuthorizedClientRegistrationId()).thenReturn(clientRegistrationId);

        when(authorizedClientService.loadAuthorizedClient(
                eq(clientRegistrationId),
                eq(principalName)
        )).thenReturn(authorizedClient);

        when(authorizedClient.getPrincipalName()).thenReturn(principalName);

        assertDoesNotThrow(() -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        verify(authorizedClientService).loadAuthorizedClient(clientRegistrationId, principalName);
    }

    @Test
    void shouldHandleNullPrincipalName() throws IOException {
        String clientRegistrationId = "oidc-client";
        String principalName = null;

        when(oauth2AuthenticationToken.getName()).thenReturn(principalName);
        when(oauth2AuthenticationToken.getAuthorizedClientRegistrationId()).thenReturn(clientRegistrationId);

        when(authorizedClientService.loadAuthorizedClient(
                eq(clientRegistrationId),
                eq(principalName)
        )).thenReturn(authorizedClient);

        when(authorizedClient.getPrincipalName()).thenReturn(principalName);

        assertDoesNotThrow(() -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        verify(authorizedClientService).loadAuthorizedClient(clientRegistrationId, principalName);
    }

    @Test
    void shouldHandleNullClientRegistrationId() throws IOException {
        String clientRegistrationId = null;
        String principalName = "testuser@example.com";

        when(oauth2AuthenticationToken.getName()).thenReturn(principalName);
        when(oauth2AuthenticationToken.getAuthorizedClientRegistrationId()).thenReturn(clientRegistrationId);

        when(authorizedClientService.loadAuthorizedClient(
                eq(clientRegistrationId),
                eq(principalName)
        )).thenReturn(authorizedClient);

        when(authorizedClient.getPrincipalName()).thenReturn(principalName);

        assertDoesNotThrow(() -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        verify(authorizedClientService).loadAuthorizedClient(clientRegistrationId, principalName);
    }

    @Test
    void shouldHandleAuthenticationSuccessWithRealOAuth2Token() throws IOException {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("oidc-client")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/oidc-client")
                .authorizationUri("https://example.com/oauth2/authorize")
                .tokenUri("https://example.com/oauth2/token")
                .userInfoUri("https://example.com/userinfo")
                .userNameAttributeName("sub")
                .build();

        OidcIdToken idToken = new OidcIdToken(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9...",
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(3600),
                Map.of(
                        "sub", "testuser@example.com",
                        "email", "testuser@example.com",
                        "name", "Test User"
                )
        );

        OidcUser oidcUser = new DefaultOidcUser(
                Collections.emptyList(),
                idToken
        );

        OAuth2AuthenticationToken realOAuth2Token = new OAuth2AuthenticationToken(
                oidcUser,
                Collections.emptyList(),
                "oidc-client"
        );

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        OAuth2AuthorizedClient realAuthorizedClient = new OAuth2AuthorizedClient(
                clientRegistration,
                "testuser@example.com",
                accessToken
        );

        when(authorizedClientService.loadAuthorizedClient(
                eq("oidc-client"),
                eq("testuser@example.com")
        )).thenReturn(realAuthorizedClient);

        assertDoesNotThrow(() -> {
            handler.onAuthenticationSuccess(request, response, realOAuth2Token);
        });

        verify(authorizedClientService).loadAuthorizedClient("oidc-client", "testuser@example.com");
    }

    @Test
    void shouldHandleExceptionFromAuthorizedClientService() throws IOException {
        String clientRegistrationId = "oidc-client";
        String principalName = "testuser@example.com";

        when(oauth2AuthenticationToken.getName()).thenReturn(principalName);
        when(oauth2AuthenticationToken.getAuthorizedClientRegistrationId()).thenReturn(clientRegistrationId);

        when(authorizedClientService.loadAuthorizedClient(
                eq(clientRegistrationId),
                eq(principalName)
        )).thenThrow(new RuntimeException("Service unavailable"));

        assertThrows(RuntimeException.class, () -> {
            handler.onAuthenticationSuccess(request, response, oauth2AuthenticationToken);
        });

        verify(authorizedClientService).loadAuthorizedClient(clientRegistrationId, principalName);
    }
}