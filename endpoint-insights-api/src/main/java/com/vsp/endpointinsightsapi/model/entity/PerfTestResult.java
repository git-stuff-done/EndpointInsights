package com.vsp.endpointinsightsapi.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "perf_test_result")
public class PerfTestResult {
	@EmbeddedId
	private PerfTestResultId id;

	@MapsId("resultId")
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "result_id", nullable = false, insertable = false, updatable = false)
	private TestResult testResult;

	@Column(name = "sampler_name", nullable = false, insertable = false, updatable = false)
	private String samplerName;

	@Column(name = "thread_group", nullable = false, insertable = false, updatable = false)
	private String threadGroup;

	@Column(name = "p50_latency_ms")
	private Integer p50LatencyMs;

	@Column(name = "p95_latency_ms")
	private Integer p95LatencyMs;

	@Column(name = "p99_latency_ms")
	private Integer p99LatencyMs;

	@Column(name = "volume_last_minute")
	private Integer volumeLastMinute;

	@Column(name = "volume_last_5_minutes")
	private Integer volumeLast5Minutes;

	@Column(name = "error_rate_percent")
	private Double errorRatePercent;

}