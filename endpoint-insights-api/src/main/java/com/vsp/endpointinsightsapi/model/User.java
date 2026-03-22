package com.vsp.endpointinsightsapi.model;

import com.vsp.endpointinsightsapi.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Persistent user entity stored in the database.
 *
 * <p>Users are uniquely identified by their OIDC issuer (iss) and subject (subject) claims.
 * User details are automatically created or updated when they authenticate via OIDC.
 *
 * <h2>Identity:</h2>
 * <ul>
 *   <li><strong>iss</strong> - OIDC issuer URL (e.g., https://auth.example.com)</li>
 *   <li><strong>subject</strong> - OIDC subject claim (unique user ID within the issuer)</li>
 * </ul>
 *
 * <p>The combination of iss/subject uniquely identifies a user across all identity providers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "users_issuer_subject_key", columnNames = {"issuer", "subject"})
})
public class User extends AuditingEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id")
    private UUID id;

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
     * Display name from OIDC preferred_username or name claim.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Email address from OIDC email claim.
     */
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * User's role/permission level in the application.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    /**
     * Returns the OIDC identity in standard format: "issuer/subject"
     */
    public String getOidcIdentity() {
        return issuer + "/" + subject;
    }
}
