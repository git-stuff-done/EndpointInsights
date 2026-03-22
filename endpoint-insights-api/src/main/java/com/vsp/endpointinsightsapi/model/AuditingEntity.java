package com.vsp.endpointinsightsapi.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base class for all audited entities.
 *
 * <p>Automatically tracks who created/modified the entity and when, using
 * OIDC identity (issuer/subject) for user tracking.
 */
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class AuditingEntity {

    @CreatedBy
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "issuer", column = @Column(name = "created_by_issuer")),
        @AttributeOverride(name = "subject", column = @Column(name = "created_by_subject"))
    })
    private UserIdentity createdBy;

    @CreatedDate
    @Column(name = "created_date")
    private Instant createdDate;

    @LastModifiedBy
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "issuer", column = @Column(name = "updated_by_issuer", nullable = false)),
        @AttributeOverride(name = "subject", column = @Column(name = "updated_by_subject", nullable = false))
    })
    private UserIdentity updatedBy;

    @LastModifiedDate
    @Column(name = "updated_date", nullable = false)
    private Instant updatedDate;

}
