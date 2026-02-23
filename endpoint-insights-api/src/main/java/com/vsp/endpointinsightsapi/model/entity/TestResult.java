package com.vsp.endpointinsightsapi.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "test_result")
public class TestResult {
	@Id
	@ColumnDefault("gen_random_uuid()")
	@Column(name = "result_id", nullable = false)
	private UUID id;

	@Column(name = "job_type")
	private Integer jobType;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

}