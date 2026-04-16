package com.vsp.endpointinsightsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationGroupDTO {
    private UUID id;
    private String name;
    private String description;
    private List<String> members;
}
