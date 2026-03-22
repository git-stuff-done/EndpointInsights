package com.vsp.endpointinsightsapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Embeddable composite key representing a user's OIDC identity.
 *
 * <p>Users are uniquely identified by the combination of their OIDC issuer (iss)
 * and subject (sub) claims. This class encapsulates that identity for use in
 * foreign key relationships and audit tracking.
 *
 * <h2>Usage:</h2>
 * <ul>
 *   <li>Embedded in audit fields (createdBy, updatedBy)</li>
 *   <li>References to the User entity</li>
 * </ul>
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentity implements Serializable {

    /**
     * OIDC issuer claim - identifies the identity provider.
     */
    @Column(name = "issuer", nullable = false, length = 512)
    private String issuer;

    /**
     * OIDC subject claim - unique user identifier within the issuer.
     */
    @Column(name = "subject", nullable = false, length = 512)
    private String subject;

    /**
     * Returns the OIDC identity in standard format: "issuer/subject"
     */
    @Override
    public String toString() {
        return issuer + "/" + subject;
    }
}
