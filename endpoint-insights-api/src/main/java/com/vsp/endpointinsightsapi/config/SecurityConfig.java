package com.vsp.endpointinsightsapi.config;

import com.vsp.endpointinsightsapi.authentication.OAuth2JsonSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;


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



    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(this.oidcClientRegistration());
    }

    private ClientRegistration oidcClientRegistration() {
        return ClientRegistration.withRegistrationId("oidc")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .issuerUri(issuerUri)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("openid", "profile", "email", "groups")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .clientName("OIDC")
                .redirectUri(redirectUri)
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .userInfoUri(userInfoUri)
                .userNameAttributeName(userNameAttribute)
                .jwkSetUri(jwkSetUri)
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/**", "/login/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
        )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2JsonSuccessHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                );

        return http.build();
    }
}
