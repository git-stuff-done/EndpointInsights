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

	@Column(name = "count")
	private Integer count;

}