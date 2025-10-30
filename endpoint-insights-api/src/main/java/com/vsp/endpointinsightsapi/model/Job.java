package com.vsp.endpointinsightsapi.model;

import jakarta.persistence.*;

import java.util.Date;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//import com.vsp.endpointinsightsapi.user.User;           // adjust imports/package names for when created
//import com.vsp.endpointinsightsapi.target.TestTarget;  // adjust imports/package names for when created
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import java.util.Map;
import com.vsp.endpointinsightsapi.model.enums.JobStatus;
import com.vsp.endpointinsightsapi.model.enums.TestType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job")
public class Job {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "job_id")
    private String jobId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false, length = 20)
    private TestType testType;

    // Uncomment when the TestTarget and User Entities are created
    /*
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id")
    private TestTarget testTarget;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;
*/
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Column(name = "started_at", nullable = false)
    private Date startedAt;

    @Column(name = "completed_at", nullable = false)
    private Date completedAt;

    // JSONB config: arbitrary key/value settings for the job
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", columnDefinition = "jsonb")
    private Map<String, Object> config;


    @PrePersist
    void onCreate() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = new Date();
    }
}
