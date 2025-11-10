package com.vsp.endpointinsightsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchRequestDTO {
    private String name;
    private Long scheduleId;
    private LocalDate startTime;
    private Boolean active;
}
