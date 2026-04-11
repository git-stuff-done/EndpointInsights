package com.vsp.endpointinsightsapi.authentication;

import com.vsp.endpointinsightsapi.config.AuthenticationProperties;
import com.vsp.endpointinsightsapi.controller.HealthController;
import com.vsp.endpointinsightsapi.exception.CustomException;
import com.vsp.endpointinsightsapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthorizationInterceptorUnitTest {

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private Jwt mockJwt;

    @Mock
    private AuthenticationProperties.Claims claimsConfig;

    @Mock
    private AuthenticationProperties.Groups groupsConfig;

    @Mock
    private AuthenticationProperties authProperties;

    @Mock
    private UserService userService;

    private AuthorizationInterceptor interceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        interceptor = new AuthorizationInterceptor(authProperties, userService);
    }

    @Test
    void shouldAllowHealthEndpoint() throws Exception {
        request.setRequestURI("/api/health");

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheck"));

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result, "Public endpoints should be accessible without authentication");
    }

    @Test
    void shouldRejectPrivateEndpointWithoutAuthorizationHeader() throws Exception {
        request.setRequestURI("/api/private");

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Private endpoints should reject requests without authorization header");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void shouldRejectRequestWithWrongAuthorizationTokenType() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Basic auth");

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void shouldRejectRequestWithWhitespaceToken() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer  ");

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void shouldRejectRequestWithInvalidBearerFormat() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer invalid.jwt.token");

        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);

        when(jwtDecoder.decode(anyString())).thenThrow(new JwtException("Invalid token"));

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void shouldRejectRequestWithBearerButNoToken() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer ");

        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject authorization header with 'Bearer ' but no token");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void shouldRejectJwtWithMissingSubjectClaim() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn(null);

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with missing subject claim");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void shouldRejectJwtWithWhitespaceSubjectClaim() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("  ");

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with missing subject claim");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void shouldRejectJwtWithMissingUsernameClaim() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with missing username claim");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());

        InOrder inOrder = inOrder(mockJwt);
        inOrder.verify(mockJwt, times(1)).getClaims();
        inOrder.verify(mockJwt, times(1)).getAudience();
    }

    @Test
    void shouldRejectJwtWithMissingEmailClaim() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with missing email claim");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());

        InOrder inOrder = inOrder(mockJwt);
        inOrder.verify(mockJwt, times(1)).getClaims();
        inOrder.verify(mockJwt, times(1)).getAudience();
        verify(mockJwt, never()).getClaimAsStringList(anyString());
    }

    @Test
    void shouldSuccessfullyAuthenticateValidJwtWithReadRole() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(mock(AuthenticationProperties.Groups.class));
        when(authProperties.getGroups().getRead()).thenReturn("read-group");
        when(authProperties.getGroups().getWrite()).thenReturn("write-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("read-group"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "Valid JWT should allow access");
        assertEquals("valid.jwt.token", request.getAttribute("bearer-token"));
        assertNotNull(request.getAttribute("jwt"));

        verify(jwtDecoder).decode("valid.jwt.token");
        verify(mockJwt, times(4)).getSubject();
        verify(mockJwt, times(2)).getClaimAsString("preferred_username");
        verify(mockJwt, times(2)).getClaimAsString("email");
        verify(mockJwt).getAudience();
        verify(mockJwt).getClaimAsStringList("groups");
    }

    @Test
    void shouldSuccessfullyAuthenticateValidJwtWithWriteRole() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getWrite()).thenReturn("write-group");
        when(groupsConfig.getRead()).thenReturn("read-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("write-group"));

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result, "Valid JWT should allow access");
        assertEquals("valid.jwt.token", request.getAttribute("bearer-token"));
        assertNotNull(request.getAttribute("jwt"));

        verify(jwtDecoder).decode("valid.jwt.token");
        verify(mockJwt, times(4)).getSubject();
        verify(mockJwt, times(2)).getClaimAsString("preferred_username");
        verify(mockJwt, times(2)).getClaimAsString("email");
        verify(mockJwt).getAudience();
        verify(mockJwt).getClaimAsStringList("groups");
    }

    @Test
    void shouldSuccessfullyAuthenticateValidJwtWithNoGroups() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, new Object());
        }, "Should reject user with no groups");

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    }


    @Test
    void shouldSetUserContextCorrectly() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(mock(AuthenticationProperties.Groups.class));
        when(authProperties.getGroups().getRead()).thenReturn("read-group");
        when(authProperties.getGroups().getWrite()).thenReturn("write-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("read-group"));

        interceptor.preHandle(request, response, new Object());

        assertEquals("valid.jwt.token", request.getAttribute("bearer-token"));
        assertNotNull(request.getAttribute("jwt"));
    }

    @Test
    void shouldHandleEmptyGroupsList() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of());

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, new Object());
        }, "Valid JWT with empty groups list should allow access");

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    }

    @Test
    void shouldHandleUnrecognizedGroups() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(mock(AuthenticationProperties.Groups.class));
        when(authProperties.getGroups().getRead()).thenReturn("read-group");
        when(authProperties.getGroups().getWrite()).thenReturn("write-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("unknown-group", "another-group"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, new Object());
        }, "Valid JWT with unrecognized groups should allow access");

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    }

    @Test
    void shouldRejectValidJwtWithNoGroups_WithAuthorizationEnabled() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(null);  // No groups = NONE role

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, new Object());
        }, "Should reject JWT without required read/write groups");

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    }

    @Test
    void shouldRejectValidJwtWithUnrecognizedGroups_WithAuthorizationEnabled() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("unknown-group", "another-group"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, new Object());
        }, "Should reject JWT with groups that don't grant read/write access");

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
    }

    @Test
    void shouldAllowValidJwtWithReadRole() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("read-group"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "Valid JWT with read role should allow access");
    }

    @Test
    void shouldAllowValidJwtWithWriteRole() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getWrite()).thenReturn("write-group");
        when(groupsConfig.getRead()).thenReturn("read-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("write-group"));

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result, "Valid JWT with write role should allow access");
    }

    @Test
    void shouldAllowJwtWithMatchingClientIdAudience() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("read-group"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "JWT with audience matching client ID should be allowed");
        verify(mockJwt).getAudience();
    }

    @Test
    void shouldAllowJwtWithAudienceInAllowedAudiences() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");
        when(authProperties.getAllowedAudiences()).thenReturn(List.of("external-api", "partner-service"));

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("external-api"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("read-group"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "JWT with audience in allowed audiences list should be allowed");
        verify(mockJwt).getAudience();
    }

    @Test
    void shouldAllowJwtWithMultipleAudiencesIncludingClientId() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");
        when(authProperties.getAllowedAudiences()).thenReturn(List.of());

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("other-service", "endpoint-insights", "another-service"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("read-group"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "JWT with multiple audiences including client ID should be allowed");
        verify(mockJwt).getAudience();
    }

    @Test
    void shouldAllowJwtWithMultipleAudiencesIncludingAllowedAudience() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");
        when(authProperties.getAllowedAudiences()).thenReturn(List.of("partner-service"));

        when(mockJwt.getClaimAsString("client_id")).thenReturn(null);
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("other-service", "partner-service", "another-service"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("read-group"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "JWT with multiple audiences including allowed audience should be allowed");
        verify(mockJwt).getAudience();
    }

    @Test
    void shouldRejectJwtWithMissingAudienceClaim() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);

        when(mockJwt.getAudience()).thenReturn(null);

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with missing audience claim");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        verify(mockJwt).getAudience();
    }

    @Test
    void shouldRejectJwtWithEmptyAudienceClaim() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);

        when(mockJwt.getAudience()).thenReturn(List.of());

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with empty audience claim");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        verify(mockJwt).getAudience();
    }

    @Test
    void shouldRejectJwtWithNonMatchingAudience() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);

        when(mockJwt.getAudience()).thenReturn(List.of("wrong-audience"));
        when(authProperties.getAllowedAudiences()).thenReturn(List.of("partner-service"));

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with non-matching audience");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        verify(mockJwt).getAudience();
    }

    @Test
    void shouldRejectJwtWithMultipleNonMatchingAudiences() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);

        when(mockJwt.getAudience()).thenReturn(List.of("wrong-audience-1", "wrong-audience-2", "wrong-audience-3"));
        when(authProperties.getAllowedAudiences()).thenReturn(List.of("partner-service"));

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with multiple non-matching audiences");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        verify(mockJwt).getAudience();
    }

    @Test
    void shouldAcceptClientCredentialsTokenWithValidAudience() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer client.credentials.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("client.credentials.token")).thenReturn(mockJwt);

        when(mockJwt.getClaimAsString("client_id")).thenReturn("jmeter-service-account");
        when(mockJwt.getSubject()).thenReturn(null);
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "Client credentials token should be accepted");
        assertEquals("client.credentials.token", request.getAttribute("bearer-token"));
        assertNotNull(request.getAttribute("jwt"));

        verify(jwtDecoder).decode("client.credentials.token");
        verify(mockJwt).getAudience();
        verify(mockJwt, atLeastOnce()).getClaimAsString("client_id");
        verify(mockJwt, atLeastOnce()).getSubject();

        verify(mockJwt, never()).getClaimAsString("preferred_username");
        verify(mockJwt, never()).getClaimAsString("email");
        verify(mockJwt, never()).getClaimAsStringList("groups");
    }

    @Test
    void shouldGrantWriteAccessToClientCredentialsToken() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer client.credentials.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("client.credentials.token")).thenReturn(mockJwt);

        when(mockJwt.getClaimAsString("client_id")).thenReturn("jmeter-client");
        when(mockJwt.getSubject()).thenReturn(null);
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "Client credentials should have write access");
    }

    @Test
    void shouldAcceptClientCredentialsTokenWithAllowedAudience() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer client.credentials.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("client.credentials.token")).thenReturn(mockJwt);
        when(authProperties.getAllowedAudiences()).thenReturn(List.of("external-client-id"));

        when(mockJwt.getClaimAsString("client_id")).thenReturn("external-service");
        when(mockJwt.getSubject()).thenReturn(null);
        when(mockJwt.getAudience()).thenReturn(List.of("external-client-id"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "Client credentials token with allowed audience should be accepted");
    }

    @Test
    void shouldAcceptClientCredentialsTokenWithMultipleAudiences() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer client.credentials.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("client.credentials.token")).thenReturn(mockJwt);

        when(mockJwt.getClaimAsString("client_id")).thenReturn("multi-audience-client");
        when(mockJwt.getSubject()).thenReturn(null);
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights", "other-service"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "Client credentials token with multiple audiences should be accepted");
    }

    @Test
    void shouldRejectClientCredentialsTokenWithInvalidAudience() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer client.credentials.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("client.credentials.token")).thenReturn(mockJwt);
        when(authProperties.getAllowedAudiences()).thenReturn(List.of());

        when(mockJwt.getAudience()).thenReturn(List.of("wrong-audience"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, new Object());
        }, "Should reject client credentials token with invalid audience");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void shouldRejectClientCredentialsTokenWithMissingAudience() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer client.credentials.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("client.credentials.token")).thenReturn(mockJwt);

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, new Object());
        }, "Should reject client credentials token with missing audience");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void shouldDistinguishBetweenUserTokenAndClientCredentialsToken() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer user.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("user.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");

        when(mockJwt.getClaimAsString("client_id")).thenReturn("some-client");
        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("read-group"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "Token with sub claim should be treated as user token");

        verify(mockJwt, atLeastOnce()).getClaimAsString("preferred_username");
        verify(mockJwt, atLeastOnce()).getClaimAsString("email");
        verify(mockJwt).getClaimAsStringList("groups");
    }

    @Test
    void shouldHandleClientCredentialsTokenWithEmptySubject() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer client.credentials.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);
        ReflectionTestUtils.setField(interceptor, "oidcClientId", "endpoint-insights");

        when(jwtDecoder.decode("client.credentials.token")).thenReturn(mockJwt);

        when(mockJwt.getClaimAsString("client_id")).thenReturn("service-client");
        when(mockJwt.getSubject()).thenReturn("  ");
        when(mockJwt.getAudience()).thenReturn(List.of("endpoint-insights"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "Client credentials token with empty subject should be accepted");
        verify(mockJwt, never()).getClaimAsString("preferred_username");
        verify(mockJwt, never()).getClaimAsString("email");
    }
}
