package com.vsp.endpointinsightsapi.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchRequestDTO {
    private String name;
    private List<UUID> jobIds;
}
