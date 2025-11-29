package com.vsp.endpointinsightsapi.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsp.endpointinsightsapi.config.AuthenticationProperties;
import com.vsp.endpointinsightsapi.exception.CustomExceptionBuilder;
import jakarta.servlet.http.Cookie;
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

/**
 * Handles successful OIDC authentication and returns JWT token details as JSON.
 *
 * <p>This handler is invoked after successful OAuth2/OIDC authentication completes.
 * Instead of redirecting to a page, it returns a JSON response containing the ID token
 * that the frontend can store and use for subsequent API requests.
 *
 * <h2>Response Format:</h2>
 * <pre>{@code
 * {
 *   "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "expiresAt": 1234567890,
 *   "username": "testuser",
 *   "email": "test@example.com"
 * }
 * }</pre>
 *
 * <h2>Validations:</h2>
 * <ul>
 *   <li>Ensures authentication is OAuth2AuthenticationToken</li>
 *   <li>Ensures principal is OidcUser (not generic OAuth2User)</li>
 *   <li>Validates ID token is present</li>
 *   <li>Validates required claims (username, email, expiration)</li>
 * </ul>
 *
 * <h2>Error Handling:</h2>
 * <p>Returns 400 Bad Request if:
 * <ul>
 *   <li>Authentication type is invalid</li>
 *   <li>ID token is missing</li>
 *   <li>Required claims are missing or empty</li>
 * </ul>
 *
 * @see SecurityConfig
 * @see AuthenticationProperties
 */
@Component
public class OAuth2JsonSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2JsonSuccessHandler.class);
    private final ObjectMapper objectMapper;

    private final AuthenticationProperties authProperties;

    public OAuth2JsonSuccessHandler(AuthenticationProperties authProperties) {
        this.authProperties = authProperties;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Processes successful OIDC authentication and returns JSON response with token details.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authentication the authentication object from Spring Security
     * @throws IOException if response writing fails
     * @throws CustomException with 400 if authentication or token validation fails
     */
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

        String username = oidcUser.getAttribute(authProperties.getClaims().getUsername());
        String email = oidcUser.getAttribute(authProperties.getClaims().getEmail());
        Instant expiresAt = idToken.getExpiresAt();

        if (username == null || username.trim().isEmpty()) {
            LOG.error("ID token missing preferred_username for user '{}'", oidcUser.getName());
            throw new CustomExceptionBuilder()
                    .withStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }

        if (email == null || email.trim().isEmpty()) {
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

        Cookie tokenCookie = new Cookie("authToken", idToken.getTokenValue());
        //todo: set to secure only once we implement tls
//        tokenCookie.setSecure(true);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge((int) (expiresAt.getEpochSecond() - Instant.now().getEpochSecond()));

        response.addCookie(tokenCookie);
        response.sendRedirect(authProperties.getCallbackUri());
    }

}