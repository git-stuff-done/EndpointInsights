package com.vsp.endpointinsightsapi.util;

import com.vsp.endpointinsightsapi.model.UserContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Utility class for accessing the current user's context from anywhere in the application.
 *
 * <p>This class provides static methods to access {@link UserContext} that is set by
 * {@link com.vsp.endpointinsightsapi.authentication.AuthorizationInterceptor} during request processing.
 * The context is stored in request-scoped attributes via Spring's {@link RequestContextHolder},
 * ensuring thread-safety and automatic cleanup after request completion.
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Get full user context
 * Optional<UserContext> context = CurrentUser.get();
 *
 * // Get specific user properties (returns "system" if no user)
 * String userId = CurrentUser.getUserId();
 * String username = CurrentUser.getUsername();
 * String logIdentifier = CurrentUser.getLogIdentifier();
 *
 * // Check permissions
 * if (CurrentUser.hasWriteAccess()) {
 *     // perform write operation
 * }
 * }</pre>
 *
 * <h2>Availability:</h2>
 * <ul>
 *   <li>Available for all protected API endpoints after JWT validation</li>
 *   <li>Returns empty/default values for public endpoints</li>
 *   <li>Returns empty if called outside of a web request context</li>
 * </ul>
 *
 * @see UserContext
 * @see com.vsp.endpointinsightsapi.authentication.AuthorizationInterceptor
 */
public class CurrentUser {

    private static final String USER_CONTEXT_ATTRIBUTE = "userContext";

    /**
     * Gets the current user's context from the request.
     *
     * @return Optional containing UserContext if available, empty otherwise
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
     * Gets the current user's ID for logging purposes.
     *
     * @return the user ID, or "system" if no user context is available
     */
    public static String getUserId() {
        return get().map(UserContext::getUserId).orElse("system");
    }

    /**
     * Gets the current user's username for logging purposes.
     *
     * @return the username, or "system" if no user context is available
     */
    public static String getUsername() {
        return get().map(UserContext::getUsername).orElse("system");
    }

    /**
     * Gets a user identifier suitable for logging (username + ID).
     *
     * @return formatted log identifier "username (userId)", or "system" if unavailable
     */
    public static String getLogIdentifier() {
        return get().map(UserContext::getLogIdentifier).orElse("system");
    }

    /**
     * Checks if the current user has write access.
     *
     * @return true if user has WRITE role, false otherwise or if no user context
     */
    public static boolean hasWriteAccess() {
        return get().map(UserContext::hasWriteAccess).orElse(false);
    }

    /**
     * Checks if the current user has read access.
     *
     * @return true if user has READ or WRITE role, false otherwise or if no user context
     */
    public static boolean hasReadAccess() {
        return get().map(UserContext::hasReadAccess).orElse(false);
    }

    /**
     * Sets the user context for the current request.
     *
     * <p><strong>Internal use only.</strong> This method is called by
     * {@link com.vsp.endpointinsightsapi.authentication.AuthorizationInterceptor}
     * and should not be called from application code.
     *
     * @param userContext the user context to set
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