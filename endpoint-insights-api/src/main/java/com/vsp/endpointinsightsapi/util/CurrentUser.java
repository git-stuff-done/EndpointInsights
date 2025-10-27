package com.vsp.endpointinsightsapi.util;

import com.vsp.endpointinsightsapi.model.UserContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Utility class for accessing the current user's context from anywhere in the application.
 * Prevents unnecessary json unmarshalling.
 */
public class CurrentUser {

    private static final String USER_CONTEXT_ATTRIBUTE = "userContext";

    /**
     * Get the current user's context from the request.
     * Returns empty if no user context is available (e.g., public endpoints).
     */
    public static Optional<UserContext> get() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            UserContext userContext = (UserContext) request.getAttribute(USER_CONTEXT_ATTRIBUTE);
            return Optional.ofNullable(userContext);
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    /**
     * Get the current user's ID for logging purposes.
     * Returns "system" if no user context is available.
     */
    public static String getUserId() {
        return get().map(UserContext::getUserId).orElse("system");
    }

    /**
     * Get the current user's username for logging purposes.
     * Returns "system" if no user context is available.
     */
    public static String getUsername() {
        return get().map(UserContext::getUsername).orElse("system");
    }

    /**
     * Get a user identifier suitable for logging (username + ID).
     * Returns "system" if no user context is available.
     */
    public static String getLogIdentifier() {
        return get().map(UserContext::getLogIdentifier).orElse("system");
    }

    /**
     * Check if the current user has write access.
     * Returns false if no user context is available.
     */
    public static boolean hasWriteAccess() {
        return get().map(UserContext::hasWriteAccess).orElse(false);
    }

    /**
     * Check if the current user has read access.
     * Returns false if no user context is available.
     */
    public static boolean hasReadAccess() {
        return get().map(UserContext::hasReadAccess).orElse(false);
    }

    /**
     * Internal method for the interceptor to set the user context.
     * Should not be called from application code.
     */
    public static void setUserContext(UserContext userContext) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            request.setAttribute(USER_CONTEXT_ATTRIBUTE, userContext);
        } catch (IllegalStateException _) {
        }
    }
}