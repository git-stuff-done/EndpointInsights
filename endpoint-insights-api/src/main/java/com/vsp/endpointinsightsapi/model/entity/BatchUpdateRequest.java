package com.vsp.endpointinsightsapi.model.entity;

import java.util.List;
import java.util.UUID;

public class BatchUpdateRequest {

	public String cronExpression;
	public List<UUID> addJobs;
	public List<UUID> deleteJobs;

}
