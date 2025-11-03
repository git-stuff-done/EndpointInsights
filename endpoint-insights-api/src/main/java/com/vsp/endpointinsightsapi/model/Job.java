package com.vsp.endpointinsightsapi.model;
import com.vsp.endpointinsightsapi.model.enums.JobStatus;
import com.vsp.endpointinsightsapi.model.enums.TestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job")
public class Job {

    @Id
    @ColumnDefault("get_random_uuid()")
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false, length = 20)
    private TestType testType;

	@ManyToMany
	@JoinTable(
			name = "test_batch_tests",
			joinColumns = @JoinColumn(name = "job_id", columnDefinition = "uuid"),
			inverseJoinColumns = @JoinColumn(name = "test_job_id", columnDefinition = "uuid")
	)
	//private Set<TestBatch> testBatches;

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
