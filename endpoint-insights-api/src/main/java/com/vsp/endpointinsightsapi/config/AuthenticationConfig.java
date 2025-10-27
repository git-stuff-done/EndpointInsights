package com.vsp.endpointinsightsapi.config;

import com.vsp.endpointinsightsapi.authentication.AuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ConditionalOnProperty(name = "app.authentication.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@Order(1)
@RequiredArgsConstructor
public class AuthenticationConfig implements WebMvcConfigurer {

	private final AuthenticationInterceptor authenticationInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authenticationInterceptor).addPathPatterns("/api/**");
	}
}
