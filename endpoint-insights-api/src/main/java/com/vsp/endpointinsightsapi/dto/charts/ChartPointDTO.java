package com.vsp.endpointinsightsapi.dto.charts;

import lombok.Getter;

public class ChartPointDTO {
    @Getter
    private String label;
    @Getter
    private long value;
    @Getter
    private String status;

    public ChartPointDTO(String label, long value, String status) {
        this.label = label;
        this.value = value;
        this.status = status;
    }

}
