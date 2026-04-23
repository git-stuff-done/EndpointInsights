package com.vsp.endpointinsightsapi.authentication;

import com.vsp.endpointinsightsapi.config.AuthenticationProperties;
import com.vsp.endpointinsightsapi.exception.CustomException;
import com.vsp.endpointinsightsapi.exception.CustomExceptionBuilder;
import com.vsp.endpointinsightsapi.model.UserContext;
import com.vsp.endpointinsightsapi.model.enums.UserRole;
import com.vsp.endpointinsightsapi.service.UserService;
import com.vsp.endpointinsightsapi.util.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.PostConstruct;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Validates JWT bearer tokens on API requests and enforces role-based authorization.
 *
 * <p>This interceptor validates JWT tokens received from OIDC authentication against the
 * identity provider's JWKS (JSON Web Key Set). It extracts user context from token claims
 * and makes it available throughout the request lifecycle via {@link CurrentUser}.
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Validates JWT signature against OIDC provider's JWKS</li>
 *   <li>Validates JWT audience claim against OIDC client ID and allowed audiences</li>
 *   <li>Extracts user context (subject, username, email, role) from token claims</li>
 *   <li>Enforces role-based access control based on group membership</li>
 *   <li>Allows public endpoints to bypass authentication</li>
 *   <li>Sets up {@link UserContext} accessible via {@link CurrentUser} utility</li>
 * </ul>
 *
 * <h2>HTTP Status Codes:</h2>
 * <ul>
 *   <li>401 Unauthorized - Missing, invalid, or expired JWT token</li>
 *   <li>403 Forbidden - Valid token but insufficient permissions (no matching groups)</li>
 * </ul>
 *
 * <h2>Configuration:</h2>
 * <p>Can be disabled by setting {@code app.authentication.enabled=false}</p>
 *
 * @see CurrentUser
 * @see UserContext
 * @see AuthenticationProperties
 */
@Component
@ConditionalOnProperty(name = "app.authentication.enabled", havingValue = "true", matchIfMissing = true)
public class AuthorizationInterceptor implements HandlerInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(AuthorizationInterceptor.class);

	@Value("${spring.security.oauth2.client.provider.oidc.jwk-set-uri}")
	private String jwkSetUri;

	@Value("${spring.security.oauth2.client.registration.oidc.client-id}")
	private String oidcClientId;

	private static final String jwtAttribute = "jwt";
	private static final String bearerTokenAttribute = "bearer-token";

	private final AuthenticationProperties authProperties;
	private final UserService userService;
	private JwtDecoder jwtDecoder;

	public AuthorizationInterceptor(AuthenticationProperties authProperties, UserService userService) {
		this.authProperties = authProperties;
		this.userService = userService;
	}

	/**
	 * Initializes the JWT decoder with the OIDC provider's JWKS URI.
	 * Sets up the public endpoints set from configuration.
	 * Configures the decoder to skip JWT 'typ' header validation to support various providers.
	 */
	@PostConstruct
	private void initializeJwtDecoder() {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

		OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
				new JwtTimestampValidator()
		);
		decoder.setJwtValidator(validator);

		this.jwtDecoder = decoder;

		LOG.debug("Initialized JWT decoder with JWKS URI: {}", jwkSetUri);
		LOG.debug("Authentication groups - Read: '{}', Write: '{}'",
				authProperties.getGroups().getRead(),
				authProperties.getGroups().getWrite());
	}

	/**
	 * Validates JWT bearer token and sets up user context for the request.
	 *
	 * <p>Performs the following steps:
	 * <ol>
	 *   <li>Checks if endpoint is public (bypasses authentication if true)</li>
	 *   <li>Extracts and validates Authorization header format</li>
	 *   <li>Decodes and validates JWT signature against JWKS</li>
	 *   <li>Validates required claims (subject, username, email)</li>
	 *   <li>Validates audience claim matches client ID or allowed audiences</li>
	 *   <li>Extracts user role from groups claim</li>
	 *   <li>Verifies user has at least read access</li>
	 *   <li>Creates and sets UserContext via CurrentUser</li>
	 * </ol>
	 *
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @param handler the handler for the request
	 * @return true if request should proceed, false otherwise
	 * @throws CustomException with 401 if token is missing/invalid, 403 if unauthorized
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String requestPath = request.getRequestURI();
		LOG.debug("Processing request: {}", requestPath);

		if (isPublicEndpoint(handler)) {
			return true;
		}

		if (request.getMethod().equals("OPTIONS")) {
			return true;
		}

		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}

		String token = authHeader.substring(7);
		if (token.trim().isEmpty()){
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}

		try {
			Jwt jwt = jwtDecoder.decode(token);
			LOG.debug("JWT decoded successfully. Claims: {}", jwt.getClaims().keySet());

			validateAudience(jwt);
			LOG.debug("Audience validated");

			boolean isClientCredentials = isClientCredentialsToken(jwt);
			LOG.debug("Token type: {}", isClientCredentials ? "client_credentials" : "user");

			UserContext userContext;
			List<UserRole> roles;
			String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";

			if (isClientCredentials) {
				// Client credentials: use client_id as subject
				roles = List.of(UserRole.WRITE, UserRole.READ);
				String clientId = jwt.getClaimAsString("client_id");

				userContext = UserContext.builder()
						.issuer(issuer)
						.subject(clientId)
						.username(clientId)
						.email(clientId + "@service-account")
						.roles(roles)
						.build();

				LOG.debug("Client credentials token validated for client: {} from issuer: {}", clientId, issuer);
			} else {
				// Regular user: use sub claim as subject
				validateRequiredClaims(jwt);
				LOG.debug("Required claims validated");

				roles = extractRolesFromJwt(jwt);
				LOG.debug("Extracted roles: {}", roles);

				// Test if the AUTHENTICATED user has read/write access to private endpoint.
				if (roles.isEmpty()){
					LOG.error("User has no roles - no matching groups found");
					throw new CustomExceptionBuilder()
							.withStatus(HttpStatus.FORBIDDEN)  // 403 instead of 401 since they're authenticated but not authorized
							.build();
				}

				userContext = UserContext.builder()
						.issuer(issuer)
						.subject(jwt.getSubject())
						.username(jwt.getClaimAsString(authProperties.getClaims().getUsername()))
						.email(jwt.getClaimAsString(authProperties.getClaims().getEmail()))
						.roles(roles)
						.build();

				LOG.debug("JWT validated for user: {}", userContext.getLogIdentifier());
			}

            if (!isValidRole(roles, handler, request)) {
                throw new CustomExceptionBuilder()
                        .withStatus(HttpStatus.FORBIDDEN) // 403 instead of 401 since they're authenticated but not authorized
                        .build();
            }

			CurrentUser.setUserContext(userContext);

			try {
				userService.createOrUpdateUser(userContext);
			} catch (Exception e) {
				LOG.error("Failed to persist user to database: {}", e.getMessage(), e);
			}

			request.setAttribute(jwtAttribute, jwt);
			request.setAttribute(bearerTokenAttribute, token);

			return true;

		} catch (JwtException e) {
			LOG.error("JWT validation failed: {}", e.getMessage(), e);
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}
	}

	/**
	 * Determines if a JWT is a client credentials token (machine-to-machine) or a user token.
	 *
	 * <p>Client credentials tokens have a {@code client_id} claim but no {@code sub} (subject) claim.
	 * User tokens from authorization code flow have a {@code sub} claim representing the user.
	 *
	 * @param jwt the decoded JWT token
	 * @return true if this is a client credentials token, false if it's a user token
	 */
	private boolean isClientCredentialsToken(Jwt jwt) {
		String clientId = jwt.getClaimAsString("client_id");
		String subject = jwt.getSubject();

		return clientId != null && !clientId.trim().isEmpty() && (subject == null || subject.trim().isEmpty());
	}

	/**
	 * Validates that required claims are present and non-empty in the JWT.
	 *
	 * @param jwt the decoded JWT token
	 * @throws CustomException with 401 if any required claim is missing or empty
	 */
	private void validateRequiredClaims(Jwt jwt) {
		if (jwt.getSubject() == null || jwt.getSubject().trim().isEmpty()) {
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}

		String username = jwt.getClaimAsString(authProperties.getClaims().getUsername());
		if (username == null || username.trim().isEmpty()) {
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}

		String email = jwt.getClaimAsString(authProperties.getClaims().getEmail());
		if (email == null || email.trim().isEmpty()) {
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}
	}

	/**
	 * Validates that the JWT audience claim matches either the OIDC client ID or one of the allowed audiences.
	 *
	 * <p>The audience claim can be either a single string or a list of strings per the OIDC specification.
	 * The token is considered valid if ANY of the following conditions are met:
	 * <ul>
	 *   <li>The audience is a string that matches the OIDC client ID</li>
	 *   <li>The audience is a string that matches one of the allowed audiences</li>
	 *   <li>The audience is a list containing the OIDC client ID</li>
	 *   <li>The audience is a list containing one of the allowed audiences</li>
	 * </ul>
	 *
	 * @param jwt the decoded JWT token
	 * @throws CustomException with 401 if audience claim is missing or doesn't match any allowed value
	 */
	private void validateAudience(Jwt jwt) {
		List<String> audiences = jwt.getAudience();

		if (audiences == null || audiences.isEmpty()) {
			LOG.error("JWT missing required 'aud' (audience) claim");
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}

		boolean isValidAudience = audiences.stream()
				.anyMatch(aud -> aud.equals(oidcClientId) || authProperties.getAllowedAudiences().contains(aud));

		if (!isValidAudience) {
			LOG.error("JWT audience claim does not match client ID or allowed audiences. Found: {}, Expected client ID: {}, Allowed audiences: {}",
					audiences, oidcClientId, authProperties.getAllowedAudiences());
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}

		LOG.debug("JWT audience validated successfully: {}", audiences);
	}

	/**
	 * Extracts user role from JWT groups claim.
	 *
	 * <p>Checks group membership in priority order:
	 * <ol>
	 *   <li>WRITE - if user belongs to write group</li>
	 *   <li>READ - if user belongs to read group</li>
	 *   <li>NONE - if user belongs to neither group</li>
	 * </ol>
	 *
	 * @param jwt the decoded JWT token
	 * @return the user's role based on group membership
	 */
	private List<UserRole> extractRolesFromJwt(Jwt jwt) {
		String groupsClaimName = authProperties.getClaims().getGroups();
		List<String> groups = jwt.getClaimAsStringList(groupsClaimName);

		LOG.debug("Looking for groups in claim '{}'. Found: {}", groupsClaimName, groups);
		LOG.debug("Expected read group: '{}', write group: '{}'",
				authProperties.getGroups().getRead(),
				authProperties.getGroups().getWrite());

		if (groups == null || groups.isEmpty()) {
			LOG.warn("No groups found in JWT claim '{}'", groupsClaimName);
			return List.of();
		}

		List<UserRole> roles = new ArrayList<>();

		if (groups.contains(authProperties.getGroups().getWrite()))
			roles.add(UserRole.WRITE);
		if (groups.contains(authProperties.getGroups().getRead()) || roles.contains(UserRole.WRITE))
			roles.add(UserRole.READ);

		return roles;
	}

	/**
	 * Checks if the request path is configured as a public endpoint.
	 *
	 * @param handler the request handler object (most likely the method)
	 * @return true if endpoint is public, false otherwise
	 */
	private boolean isPublicEndpoint(Object handler) {
		if (!(handler instanceof HandlerMethod))
			return false;

		var handlerMethod = (HandlerMethod) handler;
		var method = handlerMethod.getMethod();

		LOG.debug("Accessing method: {} in class: {}", method.getName(), method.getDeclaringClass().getSimpleName());


		return method.isAnnotationPresent(PublicAPI.class);
	}

	/**
	 * Checks if the user roles are valid for the current handler
	 *
	 * @param roles the user roles extracted from the jwt
	 * @param handler the request handler object (most likely the method)
	 * */
	private boolean isValidRole(List<UserRole> roles, Object handler, HttpServletRequest request) {
		if (!(handler instanceof HandlerMethod))
			return true;

		HandlerMethod handlerMethod = (HandlerMethod) handler;
		Method method = handlerMethod.getMethod();

		LOG.debug("Validating role for method: {} in class: {}", method.getName(), method.getDeclaringClass().getSimpleName());

		var annotationOptional = Arrays.stream(method.getAnnotations()).filter(a -> a instanceof RequiredRoles).findFirst();

        if (annotationOptional.isEmpty()) {
            String httpMethod = request.getMethod().toUpperCase();
            if (httpMethod.equals("GET")) {
                return roles.contains(UserRole.READ) || roles.contains(UserRole.WRITE);
            }
            return roles.contains(UserRole.WRITE);
        }

		RequiredRoles requiredRolesAnnotation = (RequiredRoles) annotationOptional.get();
		UserRole[] requiredRoles = requiredRolesAnnotation.roles();
		RoleAccessConfigurationType matchStyle = requiredRolesAnnotation.roleAccessConfigurationType();

		var roleStream = Arrays.stream(requiredRoles);

		if (Objects.requireNonNull(matchStyle) == RoleAccessConfigurationType.ONE) {
			return roleStream.anyMatch(roles::contains);
		}

		return roleStream.allMatch(roles::contains);
	}
}