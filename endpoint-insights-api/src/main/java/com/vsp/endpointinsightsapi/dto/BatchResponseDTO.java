package com.vsp.endpointinsightsapi.dto;

import com.vsp.endpointinsightsapi.model.Job;
import lombok.*;

import java.time.LocalDate;
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
    private LocalDate startTime;
    private LocalDate lastTimeRun;
    private Boolean active;
//    private List<Job> jobs; Jobs Table not created yet
}
