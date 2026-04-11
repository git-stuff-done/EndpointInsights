package com.vsp.endpointinsightsapi.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BatchResponseDTO extends AuditedDTO {
    private UUID id;
    private String batchName;
    private Long scheduleId;
    private LocalDateTime startTime;
    private LocalDateTime lastTimeRun;
    private Boolean active;
    private String cronExpression;
    private List<String> notificationList;
    private List<JobDTO> jobs;

}
