package com.vsp.endpointinsightsapi.model;

import java.util.List;
import java.util.UUID;

public class BatchUpdateRequest {

	public List<UUID> addJobs;
	public List<UUID> deleteJobs;

}
