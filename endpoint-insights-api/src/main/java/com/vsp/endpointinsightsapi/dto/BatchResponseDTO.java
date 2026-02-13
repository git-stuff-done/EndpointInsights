package com.vsp.endpointinsightsapi.dto;

import com.vsp.endpointinsightsapi.model.Job;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponseDTO {
    private UUID id;
    private String batchName;
    private Long scheduleId;
    private LocalDateTime startTime;
    private LocalDateTime lastTimeRun;
    private Boolean active;
    private List<UUID> notificationList;
    private List<Job> jobs;

}
