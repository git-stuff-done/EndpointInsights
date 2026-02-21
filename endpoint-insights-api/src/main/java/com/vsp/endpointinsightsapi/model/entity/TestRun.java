package com.vsp.endpointinsightsapi.model.entity;

import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "test_run")
public class TestRun {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(name = "run_id")
	private UUID runId;

	@Column(name = "job_id", nullable = false)
	private UUID jobId;

	@Column(name = "run_by", nullable = false)
	private String runBy;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private TestRunStatus status;

	@Column(name = "started_at")
	private Instant startedAt;

	@Column(name = "finished_at")
	private Instant finishedAt;

    @Column(name = "batch_id")
    private UUID batchId;

}
