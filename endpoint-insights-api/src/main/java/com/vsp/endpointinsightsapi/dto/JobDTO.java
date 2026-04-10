package com.vsp.endpointinsightsapi.dto;

import com.vsp.endpointinsightsapi.model.enums.GitAuthType;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.model.enums.TestType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JobDTO extends AuditedDTO {
    private UUID jobId;
    private String name;
    private String description;
    private String gitUrl;
    private TestRunStatus status;
    private GitAuthType gitAuthType;
    private String runCommand;
    private String jmeterTestName;
    private String compileCommand;
    private TestType jobType;
    private Map<String, Object> config;
    private Integer threshold;
}