package com.vsp.endpointinsightsapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class JobRunHistory {
	private List<JobRun> jobRuns;
}
