package com.vsp.endpointinsightsapi.util;

import com.vsp.endpointinsightsapi.model.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class CurrentUserUnitTest {

    @Mock
    private ServletRequestAttributes servletRequestAttributes;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private UserContext userContext;

    private MockedStatic<RequestContextHolder> requestContextHolderMock;

    @BeforeEach
    void setUp() {
        requestContextHolderMock = mockStatic(RequestContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        requestContextHolderMock.close();
    }

    @Test
    void shouldReturnUserContextWhenAvailable() {
        String userId = "user123";
        String username = "testuser";

        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(userContext);

        Optional<UserContext> result = CurrentUser.get();

        assertTrue(result.isPresent());
        assertEquals(userContext, result.get());

        requestContextHolderMock.verify(RequestContextHolder::currentRequestAttributes);
        verify(servletRequestAttributes).getRequest();
        verify(httpServletRequest).getAttribute("userContext");
    }

    @Test
    void shouldReturnEmptyWhenUserContextIsNull() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(null);

        Optional<UserContext> result = CurrentUser.get();

        assertFalse(result.isPresent());

        requestContextHolderMock.verify(RequestContextHolder::currentRequestAttributes);
        verify(servletRequestAttributes).getRequest();
        verify(httpServletRequest).getAttribute("userContext");
    }

    @Test
    void shouldReturnEmptyWhenRequestContextNotAvailable() {
        when(RequestContextHolder.currentRequestAttributes()).thenThrow(new IllegalStateException("No request context"));

        Optional<UserContext> result = CurrentUser.get();

        assertFalse(result.isPresent());

        requestContextHolderMock.verify(RequestContextHolder::currentRequestAttributes);
        verifyNoInteractions(servletRequestAttributes);
        verifyNoInteractions(httpServletRequest);
    }

    @Test
    void shouldReturnUserIdWhenUserContextExists() {
        String expectedUserId = "user456";

        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(userContext);
        when(userContext.getUserId()).thenReturn(expectedUserId);

        String result = CurrentUser.getUserId();

        assertEquals(expectedUserId, result);
        verify(userContext).getUserId();
    }

    @Test
    void shouldReturnSystemWhenUserContextDoesNotExistForUserId() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(null);

        String result = CurrentUser.getUserId();

        assertEquals("system", result);
    }

    @Test
    void shouldReturnSystemWhenExceptionOccursForUserId() {
        when(RequestContextHolder.currentRequestAttributes()).thenThrow(new IllegalStateException("No request context"));

        String result = CurrentUser.getUserId();

        assertEquals("system", result);
    }

    @Test
    void shouldReturnUsernameWhenUserContextExists() {
        String expectedUsername = "testuser";

        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(userContext);
        when(userContext.getUsername()).thenReturn(expectedUsername);

        String result = CurrentUser.getUsername();

        assertEquals(expectedUsername, result);
        verify(userContext).getUsername();
    }

    @Test
    void shouldReturnSystemWhenUserContextDoesNotExistForUsername() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(null);

        String result = CurrentUser.getUsername();

        assertEquals("system", result);
    }

    @Test
    void shouldReturnSystemWhenExceptionOccursForUsername() {
        when(RequestContextHolder.currentRequestAttributes()).thenThrow(new IllegalStateException("No request context"));

        String result = CurrentUser.getUsername();

        assertEquals("system", result);
    }

    @Test
    void shouldReturnLogIdentifierWhenUserContextExists() {
        String expectedLogIdentifier = "testuser (user123)";

        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(userContext);
        when(userContext.getLogIdentifier()).thenReturn(expectedLogIdentifier);

        String result = CurrentUser.getLogIdentifier();

        assertEquals(expectedLogIdentifier, result);
        verify(userContext).getLogIdentifier();
    }

    @Test
    void shouldReturnSystemWhenUserContextDoesNotExistForLogIdentifier() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(null);

        String result = CurrentUser.getLogIdentifier();

        assertEquals("system", result);
    }

    @Test
    void shouldReturnSystemWhenExceptionOccursForLogIdentifier() {
        when(RequestContextHolder.currentRequestAttributes()).thenThrow(new IllegalStateException("No request context"));

        String result = CurrentUser.getLogIdentifier();

        assertEquals("system", result);
    }

    @Test
    void shouldReturnTrueForWriteAccessWhenUserHasAccess() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(userContext);
        when(userContext.hasWriteAccess()).thenReturn(true);

        boolean result = CurrentUser.hasWriteAccess();

        assertTrue(result);
        verify(userContext).hasWriteAccess();
    }

    @Test
    void shouldReturnFalseForWriteAccessWhenUserDoesNotHaveAccess() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(userContext);
        when(userContext.hasWriteAccess()).thenReturn(false);

        boolean result = CurrentUser.hasWriteAccess();

        assertFalse(result);
        verify(userContext).hasWriteAccess();
    }

    @Test
    void shouldReturnFalseForWriteAccessWhenUserContextDoesNotExist() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(null);

        boolean result = CurrentUser.hasWriteAccess();

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForWriteAccessWhenExceptionOccurs() {
        when(RequestContextHolder.currentRequestAttributes()).thenThrow(new IllegalStateException("No request context"));

        boolean result = CurrentUser.hasWriteAccess();

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueForReadAccessWhenUserHasAccess() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(userContext);
        when(userContext.hasReadAccess()).thenReturn(true);

        boolean result = CurrentUser.hasReadAccess();

        assertTrue(result);
        verify(userContext).hasReadAccess();
    }

    @Test
    void shouldReturnFalseForReadAccessWhenUserDoesNotHaveAccess() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(userContext);
        when(userContext.hasReadAccess()).thenReturn(false);

        boolean result = CurrentUser.hasReadAccess();

        assertFalse(result);
        verify(userContext).hasReadAccess();
    }

    @Test
    void shouldReturnFalseForReadAccessWhenUserContextDoesNotExist() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(null);

        boolean result = CurrentUser.hasReadAccess();

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForReadAccessWhenExceptionOccurs() {
        when(RequestContextHolder.currentRequestAttributes()).thenThrow(new IllegalStateException("No request context"));

        boolean result = CurrentUser.hasReadAccess();

        assertFalse(result);
    }

    @Test
    void shouldSetUserContextSuccessfully() {
        UserContext contextToSet = mock(UserContext.class);

        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);

        CurrentUser.setUserContext(contextToSet);

        requestContextHolderMock.verify(RequestContextHolder::currentRequestAttributes);
        verify(servletRequestAttributes).getRequest();
        verify(httpServletRequest).setAttribute("userContext", contextToSet);
    }

    @Test
    void shouldHandleExceptionWhenSettingUserContext() {
        UserContext contextToSet = mock(UserContext.class);

        when(RequestContextHolder.currentRequestAttributes()).thenThrow(new IllegalStateException("No request context"));

        // Should not throw exception - method silently handles it
        try {
            CurrentUser.setUserContext(contextToSet);
            // If we reach here, no exception was thrown, which is expected
        } catch (Exception e) {
            fail("Expected no exception to be thrown, but got: " + e.getMessage());
        }

        requestContextHolderMock.verify(RequestContextHolder::currentRequestAttributes);
        verifyNoInteractions(servletRequestAttributes);
        verifyNoInteractions(httpServletRequest);
    }

    @Test
    void shouldHandleNullUserContextInSetMethod() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);

        try {
            CurrentUser.setUserContext(null);
            // If we reach here, no exception was thrown, which is expected
        } catch (Exception e) {
            fail("Expected no exception to be thrown, but got: " + e.getMessage());
        }

        requestContextHolderMock.verify(RequestContextHolder::currentRequestAttributes);
        verify(servletRequestAttributes).getRequest();
        verify(httpServletRequest).setAttribute("userContext", null);
    }

    @Test
    void shouldMaintainConsistentBehaviorAcrossMultipleCalls() {
        String userId = "consistent-user";
        String username = "consistent-username";

        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(userContext);
        when(userContext.getUserId()).thenReturn(userId);
        when(userContext.getUsername()).thenReturn(username);
        when(userContext.hasWriteAccess()).thenReturn(true);
        when(userContext.hasReadAccess()).thenReturn(true);

        // Multiple calls should return consistent results
        assertEquals(userId, CurrentUser.getUserId());
        assertEquals(username, CurrentUser.getUsername());
        assertTrue(CurrentUser.hasWriteAccess());
        assertTrue(CurrentUser.hasReadAccess());
        assertTrue(CurrentUser.get().isPresent());

        // Verify the mocks were called appropriately
        verify(userContext, times(1)).getUserId();
        verify(userContext, times(1)).getUsername();
        verify(userContext, times(1)).hasWriteAccess();
        verify(userContext, times(1)).hasReadAccess();
        verify(httpServletRequest, times(5)).getAttribute("userContext");
    }

    @Test
    void shouldHandleExceptionInGetMethodAndFallbackProperly() {
        // First call succeeds
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(userContext);
        when(userContext.getUserId()).thenReturn("test-user");

        assertEquals("test-user", CurrentUser.getUserId());

        // Second call fails due to no request context
        when(RequestContextHolder.currentRequestAttributes()).thenThrow(new IllegalStateException("Request context lost"));

        assertEquals("system", CurrentUser.getUserId());
        assertEquals("system", CurrentUser.getUsername());
        assertEquals("system", CurrentUser.getLogIdentifier());
        assertFalse(CurrentUser.hasWriteAccess());
        assertFalse(CurrentUser.hasReadAccess());
        assertFalse(CurrentUser.get().isPresent());
    }

    @Test
    void shouldVerifyCorrectAttributeNameIsUsed() {
        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getAttribute("userContext")).thenReturn(userContext);

        CurrentUser.get();

        verify(httpServletRequest).getAttribute("userContext");
        verify(httpServletRequest, never()).getAttribute("user");
        verify(httpServletRequest, never()).getAttribute("context");
        verify(httpServletRequest, never()).getAttribute("userInfo");
    }

    @Test
    void shouldSetCorrectAttributeNameInSetMethod() {
        UserContext contextToSet = mock(UserContext.class);

        when(RequestContextHolder.currentRequestAttributes()).thenReturn(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);

        CurrentUser.setUserContext(contextToSet);

        verify(httpServletRequest).setAttribute("userContext", contextToSet);
        verify(httpServletRequest, never()).setAttribute(eq("user"), any());
        verify(httpServletRequest, never()).setAttribute(eq("context"), any());
        verify(httpServletRequest, never()).setAttribute(eq("userInfo"), any());
    }
}