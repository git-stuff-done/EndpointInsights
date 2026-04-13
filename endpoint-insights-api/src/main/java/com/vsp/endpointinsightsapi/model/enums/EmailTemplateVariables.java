package com.vsp.endpointinsightsapi.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum EmailTemplateVariables {
    FAILED_TEST_VARIABLES(List.of("name", "date", "startTime", "jobName", "perfTestLatency", "threshold"));
    private final List<String> items;
}
