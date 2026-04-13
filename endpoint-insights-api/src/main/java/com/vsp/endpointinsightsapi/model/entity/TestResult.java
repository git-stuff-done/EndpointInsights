package com.vsp.endpointinsightsapi.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "test_result")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TestResult {
	@Id
	@ColumnDefault("gen_random_uuid()")
	@Column(name = "result_id", nullable = false)
	@EqualsAndHashCode.Include
	private UUID id;

	@Column(name = "job_type")
	private Integer jobType;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "run_id", referencedColumnName = "run_id", nullable = false)
	private TestRun testRun;

	@OneToOne(mappedBy = "testResult", cascade = CascadeType.REMOVE)
	private PerfTestResult perfTestResult;

}