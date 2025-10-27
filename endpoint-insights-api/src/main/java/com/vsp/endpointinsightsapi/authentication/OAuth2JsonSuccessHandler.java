package com.vsp.endpointinsightsapi.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsp.endpointinsightsapi.exception.CustomExceptionBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Component
public class OAuth2JsonSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2JsonSuccessHandler.class);
    private final ObjectMapper objectMapper;

    public OAuth2JsonSuccessHandler() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
            LOG.error("Invalid authentication token: expected '{}', got '{}", OAuth2AuthenticationToken.class.getSimpleName(), authentication.getClass().getSimpleName());
            throw new CustomExceptionBuilder()
                    .withStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        if (!(oauth2Token.getPrincipal() instanceof OidcUser oidcUser)) {
            LOG.error("Expected OidcUser principal, got: {}",
                    Objects.requireNonNull(oauth2Token.getPrincipal()).getClass().getSimpleName());
            throw new CustomExceptionBuilder()
                    .withStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        LOG.info("OAuth2 authentication success for user '{}'", oidcUser.getName());

        OidcIdToken idToken = oidcUser.getIdToken();
        if (idToken == null) {
            LOG.error("authentication missing id token for user '{}'", oidcUser.getName());
            throw new CustomExceptionBuilder()
                    .withStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        String username = oidcUser.getAttribute("preferred_username");
        String email = oidcUser.getAttribute("email");
        Instant expiresAt = idToken.getExpiresAt();

        if (username == null) {
            LOG.error("ID token missing preferred_username for user '{}'", oidcUser.getName());
            throw new CustomExceptionBuilder()
                    .withStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        if (email == null) {
            LOG.error("ID token missing email for user '{}'", oidcUser.getName());
            throw new CustomExceptionBuilder()
                    .withStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        if (expiresAt == null) {
            LOG.error("ID token missing expiry time for user '{}'", oidcUser.getName());
            throw new CustomExceptionBuilder()
                    .withStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Map<String, Object> tokenResponse = Map.of(
                "idToken", idToken.getTokenValue(),
                "expiresAt", expiresAt.getEpochSecond(),
                "username", username,
                "email", email
        );

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
    }
}