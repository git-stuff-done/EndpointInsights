package com.vsp.endpointinsightsapi.model;

<<<<<<< HEAD
import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
//import com.vsp.endpointinsightsapi.user.User;           // adjust imports/package names for when created
//import com.vsp.endpointinsightsapi.target.TestTarget;  // adjust imports/package names for when created
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;
import com.vsp.endpointinsightsapi.model.enums.JobStatus;
import com.vsp.endpointinsightsapi.model.enums.TestType;

@Getter
@Setter
@Entity
@Table(name = "job")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

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
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "started_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startedAt;

    @Column(name = "completed_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
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
=======
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Job {

	private final String jobId;

	//todo: implement
>>>>>>> develop
}
