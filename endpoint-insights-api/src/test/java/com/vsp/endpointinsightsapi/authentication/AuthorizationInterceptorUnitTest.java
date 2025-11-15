package com.vsp.endpointinsightsapi.authentication;

import com.vsp.endpointinsightsapi.config.AuthenticationProperties;
import com.vsp.endpointinsightsapi.controller.HealthController;
import com.vsp.endpointinsightsapi.exception.CustomException;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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

    private AuthorizationInterceptor interceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        interceptor = new AuthorizationInterceptor(authProperties);
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

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(mockJwt.getSubject()).thenReturn(null);

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with missing subject claim");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());

        InOrder inOrder = inOrder(mockJwt);
        inOrder.verify(mockJwt, times(1)).getSubject();
        inOrder.verify(mockJwt, never()).getClaimAsString(anyString());
    }

    @Test
    void shouldRejectJwtWithWhitespaceSubjectClaim() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(mockJwt.getSubject()).thenReturn("  ");

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with missing subject claim");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());

        InOrder inOrder = inOrder(mockJwt);
        inOrder.verify(mockJwt, times(2)).getSubject();
        inOrder.verify(mockJwt, never()).getClaimAsString(anyString());
    }

    @Test
    void shouldRejectJwtWithMissingUsernameClaim() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn(null);

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with missing username claim");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());

        InOrder inOrder = inOrder(mockJwt);
        inOrder.verify(mockJwt, times(2)).getSubject();
        inOrder.verify(mockJwt, times(1)).getClaimAsString("preferred_username");
    }

    @Test
    void shouldRejectJwtWithMissingEmailClaim() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn(null);

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        CustomException exception = assertThrows(CustomException.class, () -> {
            interceptor.preHandle(request, response, handler);
        }, "Should reject JWT with missing email claim");

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());

        InOrder inOrder = inOrder(mockJwt);
        inOrder.verify(mockJwt, times(2)).getSubject();
        inOrder.verify(mockJwt).getClaimAsString("preferred_username");
        inOrder.verify(mockJwt).getClaimAsString("email");

        verify(mockJwt, never()).getClaimAsStringList(anyString());
    }

    @Test
    void shouldSuccessfullyAuthenticateValidJwtWithReadRole() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(mock(AuthenticationProperties.Groups.class));
        when(authProperties.getGroups().getRead()).thenReturn("read-group");
        when(authProperties.getGroups().getWrite()).thenReturn("write-group");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("read-group"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "Valid JWT should allow access");
        assertEquals("valid.jwt.token", request.getAttribute("bearer-token"));
        assertNotNull(request.getAttribute("jwt"));

        verify(jwtDecoder).decode("valid.jwt.token");
        verify(mockJwt, times(3)).getSubject();
        verify(mockJwt, times(2)).getClaimAsString("preferred_username");
        verify(mockJwt, times(2)).getClaimAsString("email");
        verify(mockJwt).getClaimAsStringList("groups");
    }

    @Test
    void shouldSuccessfullyAuthenticateValidJwtWithWriteRole() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getWrite()).thenReturn("write-group");
        when(groupsConfig.getRead()).thenReturn("read-group");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("write-group"));

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result, "Valid JWT should allow access");
        assertEquals("valid.jwt.token", request.getAttribute("bearer-token"));
        assertNotNull(request.getAttribute("jwt"));

        verify(jwtDecoder).decode("valid.jwt.token");
        verify(mockJwt, times(3)).getSubject();
        verify(mockJwt, times(2)).getClaimAsString("preferred_username");
        verify(mockJwt, times(2)).getClaimAsString("email");
        verify(mockJwt).getClaimAsStringList("groups");
    }

    @Test
    void shouldSuccessfullyAuthenticateValidJwtWithNoGroups() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
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

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(mock(AuthenticationProperties.Groups.class));
        when(authProperties.getGroups().getRead()).thenReturn("read-group");
        when(authProperties.getGroups().getWrite()).thenReturn("write-group");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
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

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
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

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(mock(AuthenticationProperties.Groups.class));
        when(authProperties.getGroups().getRead()).thenReturn("read-group");
        when(authProperties.getGroups().getWrite()).thenReturn("write-group");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
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

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
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

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
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

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getRead()).thenReturn("read-group");
        when(groupsConfig.getWrite()).thenReturn("write-group");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("read-group"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "Valid JWT with read role should allow access");
    }

    @Test
    void shouldAllowValidJwtWithWriteRole() throws Exception {
        request.setRequestURI("/api/private");
        request.addHeader("Authorization", "Bearer valid.jwt.token");
        ReflectionTestUtils.setField(interceptor, "jwtDecoder", jwtDecoder);

        when(jwtDecoder.decode("valid.jwt.token")).thenReturn(mockJwt);
        when(authProperties.getClaims()).thenReturn(claimsConfig);
        when(claimsConfig.getUsername()).thenReturn("preferred_username");
        when(claimsConfig.getEmail()).thenReturn("email");
        when(claimsConfig.getGroups()).thenReturn("groups");
        when(authProperties.getGroups()).thenReturn(groupsConfig);
        when(groupsConfig.getWrite()).thenReturn("write-group");
        when(groupsConfig.getRead()).thenReturn("read-group");

        when(mockJwt.getSubject()).thenReturn("user123");
        when(mockJwt.getClaimAsString("preferred_username")).thenReturn("testuser");
        when(mockJwt.getClaimAsString("email")).thenReturn("test@example.com");
        when(mockJwt.getClaimAsStringList("groups")).thenReturn(List.of("write-group"));

        var handler = new HandlerMethod(new HealthController(), HealthController.class.getMethod("healthCheckSecure"));

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result, "Valid JWT with write role should allow access");
    }
}
