package com.vsp.endpointinsightsapi.dto.charts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ChartResponseDTO {
    private String title;
    private String xAxis;
    private List<ChartSeriesDTO> series;

    public ChartResponseDTO(String title, String xAxis, List<ChartSeriesDTO> series) {
        this.title = title;
        this.xAxis = xAxis;
        this.series = series;
    }

    @JsonProperty("xAxis")
    public String getXAxis() {
        return xAxis;
    }

}
