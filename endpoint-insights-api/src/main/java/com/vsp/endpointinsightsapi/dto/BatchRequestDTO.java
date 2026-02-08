package com.vsp.endpointinsightsapi.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vsp.endpointinsightsapi.model.Job;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchRequestDTO {
    private UUID id;
    private String batchName;
    private Long scheduleId;
    private LocalDateTime startTime;
    private LocalDateTime lastTimeRun;
    private Boolean active;
    private List<UUID> notificationList;
    private List<Job> jobs;
}
