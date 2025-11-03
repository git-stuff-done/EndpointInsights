package com.vsp.endpointinsightsapi.authentication;

import com.vsp.endpointinsightsapi.config.AuthenticationProperties;
import com.vsp.endpointinsightsapi.exception.CustomException;
import com.vsp.endpointinsightsapi.exception.CustomExceptionBuilder;
import com.vsp.endpointinsightsapi.model.UserContext;
import com.vsp.endpointinsightsapi.model.enums.UserRole;
import com.vsp.endpointinsightsapi.util.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 *   <li>Extracts user context (userId, username, email, role) from token claims</li>
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

	private static final String jwtAttribute = "jwt";
	private static final String bearerTokenAttribute = "bearer-token";

	private final AuthenticationProperties authProperties;
	private JwtDecoder jwtDecoder;
	private Set<String> publicEndpointsSet;

	public AuthorizationInterceptor(AuthenticationProperties authProperties) {
		this.authProperties = authProperties;
	}

	/**
	 * Initializes the JWT decoder with the OIDC provider's JWKS URI.
	 * Sets up the public endpoints set from configuration.
	 */
	@PostConstruct
	private void initializeJwtDecoder() {
		this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		this.publicEndpointsSet = new HashSet<>(authProperties.getEndpoints().getPublicEndpoints());

		LOG.debug("Initialized JWT decoder with JWKS URI: {}", jwkSetUri);
		LOG.debug("Authentication groups - Read: '{}', Write: '{}'",
				authProperties.getGroups().getRead(),
				authProperties.getGroups().getWrite());
		LOG.info("Public endpoints: {}", publicEndpointsSet);
	}

	/**
	 * Validates JWT bearer token and sets up user context for the request.
	 *
	 * <p>Performs the following steps:
	 * <ol>
	 *   <li>Checks if endpoint is public (bypasses authentication if true)</li>
	 *   <li>Extracts and validates Authorization header format</li>
	 *   <li>Decodes and validates JWT signature against JWKS</li>
	 *   <li>Validates required claims (sub, username, email)</li>
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

		if (isPublicEndpoint(requestPath)) {
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

			validateRequiredClaims(jwt);

			UserRole role = extractRoleFromJwt(jwt);

			// Test if the AUTHENTICATED user has read/write access to private endpoint.
			if (role == UserRole.NONE){
				throw new CustomExceptionBuilder()
						.withStatus(HttpStatus.FORBIDDEN)  // 403 instead of 401 since they're authenticated but not authorized
						.build();
			}

			UserContext userContext = UserContext.builder()
					.userId(jwt.getSubject())
					.username(jwt.getClaimAsString(authProperties.getClaims().getUsername()))
					.email(jwt.getClaimAsString(authProperties.getClaims().getEmail()))
					.role(role)
					.build();

			CurrentUser.setUserContext(userContext);

			request.setAttribute(jwtAttribute, jwt);
			request.setAttribute(bearerTokenAttribute, token);

			LOG.debug("JWT validated for user: {}", userContext.getLogIdentifier());
			return true;

		} catch (JwtException e) {
			LOG.error("JWT validation failed: {}", e.getMessage());
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}
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
	private UserRole extractRoleFromJwt(Jwt jwt) {
		List<String> groups = jwt.getClaimAsStringList(authProperties.getClaims().getGroups());
		if (groups == null) return UserRole.NONE;

		if (groups.contains(authProperties.getGroups().getWrite())) return UserRole.WRITE;
		if (groups.contains(authProperties.getGroups().getRead())) return UserRole.READ;
		return UserRole.NONE;
	}

	/**
	 * Checks if the request path is configured as a public endpoint.
	 *
	 * @param requestPath the request URI path
	 * @return true if endpoint is public, false otherwise
	 */
	private boolean isPublicEndpoint(String requestPath) {
		return publicEndpointsSet.contains(requestPath);
	}
}