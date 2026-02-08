package com.vsp.endpointinsightsapi.model;

import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TestRunCreateRequest {

	@NotNull(message = "jobId is required")
	private UUID jobId;

	@NotBlank(message = "runBy is required")
	private String runBy;

	@NotNull(message = "status is required")
	private TestRunStatus status;

	private Instant startedAt;
	private Instant finishedAt;
}
