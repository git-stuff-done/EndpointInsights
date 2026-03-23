package com.vsp.endpointinsightsapi.dto.charts;

import java.util.List;

public class ChartSeriesDTO {
    private String name;
    private List<ChartPointDTO> data;

    public ChartSeriesDTO(String name, List<ChartPointDTO> data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public List<ChartPointDTO> getData() {
        return data;
    }
}