package com.vsp.endpointinsightsapi.dto.charts;

public class ChartPointDTO {
    private String label;
    private long value;

    public ChartPointDTO(String label, long value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public long getValue() {
        return value;
    }
}
