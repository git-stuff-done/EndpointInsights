package com.vsp.endpointinsightsapi.config;

import com.vsp.endpointinsightsapi.authentication.OAuth2JsonSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configures Spring Security for OIDC authentication and hybrid authorization.
 *
 * <p>This configuration sets up the application's security architecture:
 * <ul>
 *   <li><strong>OIDC Authentication:</strong> Handles user login via OAuth2/OIDC provider</li>
 *   <li><strong>JWT Authorization:</strong> API requests authorized by
 *       {@link com.vsp.endpointinsightsapi.authentication.AuthorizationInterceptor}</li>
 * </ul>
 *
 * <h2>Security Architecture:</h2>
 * <ol>
 *   <li>User initiates login at {@code /oauth2/authorization/oidc}</li>
 *   <li>Spring Security redirects to OIDC provider for authentication</li>
 *   <li>After successful authentication, {@link OAuth2JsonSuccessHandler} returns JWT token as JSON</li>
 *   <li>Frontend stores token and includes it in API requests as Bearer token</li>
 *   <li>API requests validated by AuthorizationInterceptor (not Spring Security)</li>
 * </ol>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>CSRF disabled - API uses stateless JWT Bearer tokens</li>
 *   <li>Session creation policy: IF_REQUIRED (for OAuth2 code flow state)</li>
 *   <li>Custom success handler returns JSON instead of redirect</li>
 *   <li>API authorization delegated to interceptor for flexibility</li>
 * </ul>
 *
 * @see OAuth2JsonSuccessHandler
 * @see com.vsp.endpointinsightsapi.authentication.AuthorizationInterceptor
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2JsonSuccessHandler oauth2JsonSuccessHandler;

    public SecurityConfig(OAuth2JsonSuccessHandler oauth2JsonSuccessHandler) {
        this.oauth2JsonSuccessHandler = oauth2JsonSuccessHandler;
    }

    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.provider.oidc.authorization-uri}")
    private String authorizationUri;

    @Value("${spring.security.oauth2.client.provider.oidc.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.oidc.user-info-uri}")
    private String userInfoUri;

    @Value("${spring.security.oauth2.client.provider.oidc.user-name-attribute}")
    private String userNameAttribute;

    @Value("${spring.security.oauth2.client.provider.oidc.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.client.registration.oidc.redirect-uri}")
    private String redirectUri;



    /**
     * Creates the client registration repository for OIDC provider.
     *
     * @return repository containing OIDC client registration
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(this.oidcClientRegistration());
    }

    /**
     * Builds the OIDC client registration from configuration properties.
     *
     * @return configured OIDC client registration
     */
    private ClientRegistration oidcClientRegistration() {
        return ClientRegistration.withRegistrationId("oidc")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .issuerUri(issuerUri)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("openid", "profile", "email", "groups")
                .clientName("OIDC")
                .redirectUri(redirectUri)
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .userInfoUri(userInfoUri)
                .userNameAttributeName(userNameAttribute)
                .jwkSetUri(jwkSetUri)
                .build();
    }

    /**
     * Configures the Spring Security filter chain.
     *
     * <p><strong>CSRF:</strong> Disabled because API uses stateless JWT Bearer tokens
     *
     * <p><strong>Authorization:</strong> Permits all requests to:
     * <ul>
     *   <li>{@code /api/**} - Authorization handled by AuthorizationInterceptor</li>
     *   <li>{@code /login/**} - OAuth2 login endpoints</li>
     *   <li>{@code /oauth2/**} - OAuth2 callback endpoints</li>
     * </ul>
     *
     * <p><strong>OAuth2 Login:</strong> Uses custom success handler to return JWT as JSON
     *
     * <p><strong>Session Management:</strong> IF_REQUIRED to support OAuth2 authorization
     * code flow state parameter, but API requests remain stateless
     *
     * @param http the HTTP security configuration
     * @return the configured security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disabled: API uses stateless JWT Bearer tokens in Authorization header
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/**", "/login/**", "/oauth2/**").permitAll()  // API authorization handled by AuthorizationInterceptor
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2JsonSuccessHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // Required for OAuth2 authorization code flow state
                        .maximumSessions(1)
                );

        return http.build();
    }
}
