package com.vsp.endpointinsightsapi.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BatchUpdateRequest {
    String batchName;
	public List<UUID> addJobs;
	public List<UUID> deleteJobs;

}
