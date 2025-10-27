package com.vsp.endpointinsightsapi.authentication;

import com.vsp.endpointinsightsapi.config.AuthenticationProperties;
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
import java.util.List;

/**
 * JWT authentication interceptor that provides easy access to user context for logging and authorization.
 * Sets up UserContext that can be accessed from anywhere in the application using CurrentUser utility.
 */
@Component
@ConditionalOnProperty(name = "app.authentication.enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticationInterceptor implements HandlerInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationInterceptor.class);

	@Value("${spring.security.oauth2.client.provider.oidc.jwk-set-uri}")
	private String jwkSetUri;

	private static final String jwtAttribute ="jwt";
	private static final String bearerTokenAttribute ="bearer-token";

	private final AuthenticationProperties authProperties;
	private JwtDecoder jwtDecoder;

	public AuthenticationInterceptor(AuthenticationProperties authProperties) {
		this.authProperties = authProperties;
	}

	@PostConstruct
	private void initializeJwtDecoder() {
		this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

		LOG.info("Initialized JWT decoder with JWKS URI: {}", jwkSetUri);
		LOG.info("Authentication groups - Read: '{}', Write: '{}'",
				authProperties.getGroups().getRead(),
				authProperties.getGroups().getWrite());
	}

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

	private void validateRequiredClaims(Jwt jwt) {
		if (jwt.getSubject() == null || jwt.getSubject().trim().isEmpty()) {
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}

		if (jwt.getClaimAsString(authProperties.getClaims().getUsername()) == null) {
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}

		if (jwt.getClaimAsString(authProperties.getClaims().getEmail()) == null) {
			throw new CustomExceptionBuilder()
					.withStatus(HttpStatus.UNAUTHORIZED)
					.build();
		}
	}

	private UserRole extractRoleFromJwt(Jwt jwt) {
		List<String> groups = jwt.getClaimAsStringList(authProperties.getClaims().getGroups());
		if (groups == null) return UserRole.NONE;

		if (groups.contains(authProperties.getGroups().getWrite())) return UserRole.WRITE;
		if (groups.contains(authProperties.getGroups().getRead())) return UserRole.READ;
		return UserRole.NONE;
	}

	private boolean isPublicEndpoint(String requestPath) {
		return authProperties.getEndpoints().getPublicEndpoints()
				.stream()
				.anyMatch(requestPath::equals);
	}
}