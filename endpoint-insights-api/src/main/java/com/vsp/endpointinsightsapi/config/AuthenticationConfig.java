package com.vsp.endpointinsightsapi.config;

import com.vsp.endpointinsightsapi.authentication.AuthorizationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the {@link AuthorizationInterceptor} for JWT authorization on API endpoints.
 *
 * <p>This configuration applies the authorization interceptor to all {@code /api/**} endpoints,
 * enabling JWT token validation and user context setup for protected endpoints.
 *
 * <h2>Configuration:</h2>
 * <ul>
 *   <li>Applies to all {@code /api/**} paths</li>
 *   <li>Can be disabled by setting {@code app.authentication.enabled=false}</li>
 *   <li>Executes with {@code @Order(1)} to run early in the interceptor chain</li>
 * </ul>
 *
 * @see AuthorizationInterceptor
 * @see AuthenticationProperties
 */
@ConditionalOnProperty(name = "app.authentication.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@Order(1)
@RequiredArgsConstructor
public class AuthenticationConfig implements WebMvcConfigurer {

	private final AuthorizationInterceptor authorizationInterceptor;

	/**
	 * Registers the authorization interceptor for API endpoints.
	 *
	 * @param registry the interceptor registry
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authorizationInterceptor).addPathPatterns("/api/**");
	}
}
