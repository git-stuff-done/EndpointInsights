package com.vsp.endpointinsightsapi.model;

import com.vsp.endpointinsightsapi.model.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Immutable user session data extracted from JWT token claims.
 *
 * <p>Contains user identification and role information that is available
 * throughout the request lifecycle. Created by
 * {@link com.vsp.endpointinsightsapi.authentication.AuthorizationInterceptor}
 * during JWT validation and accessed via {@link com.vsp.endpointinsightsapi.util.CurrentUser}.
 *
 * <h2>Fields:</h2>
 * <ul>
 *   <li><strong>issuer</strong> - OIDC issuer URL from JWT 'iss' claim</li>
 *   <li><strong>subject</strong> - Unique user identifier from JWT 'subject' claim</li>
 *   <li><strong>username</strong> - Display username from JWT 'preferred_username' claim</li>
 *   <li><strong>email</strong> - User email from JWT 'email' claim</li>
 *   <li><strong>role</strong> - User role determined from JWT 'groups' claim</li>
 * </ul>
 *
 * @see com.vsp.endpointinsightsapi.util.CurrentUser
 * @see UserRole
 */
@Data
@Builder
public class UserContext {
    private final String issuer;
    private final String subject;
    private final String username;
    private final String email;
    private final List<UserRole> roles;

    /**
     * Returns a formatted identifier suitable for logging.
     *
     * @return formatted string "username (subject)"
     */
    public String getLogIdentifier() {
        return String.format("%s (%s/%s)", username, subject, issuer);
    }

    /**
     * Returns the OIDC identity in standard format: "iss/subject"
     *
     * @return formatted string "issuer/subject"
     */
    public String getOidcIdentity() {
        return issuer + "/" + subject;
    }

    /**
     * Checks if user has write access (WRITE role).
     *
     * @return true if user has WRITE role, false otherwise
     */
    public boolean hasWriteAccess() {
        return roles.contains(UserRole.WRITE);
    }

    /**
     * Checks if user has read access (READ or WRITE role).
     *
     * @return true if user has READ or WRITE role, false otherwise
     */
    public boolean hasReadAccess() {
        return roles.contains(UserRole.READ) || roles.contains(UserRole.WRITE);
    }
}