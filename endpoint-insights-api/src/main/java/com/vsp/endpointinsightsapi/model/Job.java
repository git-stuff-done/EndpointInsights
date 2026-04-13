package com.vsp.endpointinsightsapi.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vsp.endpointinsightsapi.model.enums.GitAuthType;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.model.enums.TestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job")
public class Job  extends AuditingEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "git_url")
    private String gitUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TestRunStatus status = TestRunStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "git_auth_type", length = 20)
    private GitAuthType gitAuthType = GitAuthType.NONE;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "git_username")
    private String gitUsername;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "git_password")
    private String gitPassword;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "git_ssh_private_key", columnDefinition = "text")
    private String gitSshPrivateKey;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "git_ssh_passphrase")
    private String gitSshPassphrase;

    @Column(name = "run_command")
    private String runCommand;

    @Column(name = "jmeter_test_name")
    private String jmeterTestName;

    @Column(name = "compile_command")
    private String compileCommand;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false, length = 20)
    private TestType jobType;

    @JsonBackReference
    @JsonIgnore
    @ManyToMany(mappedBy = "jobs", fetch = FetchType.LAZY)
    private List<TestBatch> batches = new ArrayList<>();

    // Uncomment when the TestTarget and User Entities are created
    /*
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id")
    private TestTarget testTarget;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;
*/

    // JSONB config: arbitrary key/value settings for the job
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", columnDefinition = "jsonb")
    private Map<String, Object> config;

    @Column(name = "threshold")
    private Integer threshold;


}
