package com.vsp.endpointinsightsapi.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "test_result")
public class TestResult {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "result_id", nullable = false)
	private Integer id;

	@Column(name = "job_type")
	private Integer jobType;

}