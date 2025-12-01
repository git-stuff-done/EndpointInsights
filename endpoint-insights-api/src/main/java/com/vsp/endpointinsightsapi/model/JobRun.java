package com.vsp.endpointinsightsapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class JobRun {

	private UUID runId;
	private String jobId;


}
