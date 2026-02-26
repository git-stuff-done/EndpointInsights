package com.vsp.endpointinsightsapi.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BatchUpdateRequest {

	public String cronExpression;
	public String batchName;
	public List<UUID> addJobs;
	public List<UUID> deleteJobs;

}
