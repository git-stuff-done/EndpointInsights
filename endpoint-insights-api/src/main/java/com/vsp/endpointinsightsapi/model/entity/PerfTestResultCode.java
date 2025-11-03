package com.vsp.endpointinsightsapi.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "perf_test_result_code")
public class PerfTestResultCode {
	@EmbeddedId
	private PerfTestResultCodeId id;

	@MapsId("resultId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "result_id", nullable = false)
	private PerfTestResult result;

	@Column(name = "count")
	private Integer count;

}