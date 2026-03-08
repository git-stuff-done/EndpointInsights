package com.vsp.endpointinsightsapi.controller;

import tools.jackson.databind.ObjectMapper;
import com.vsp.endpointinsightsapi.dto.DashboardTestActivityDTO;
import com.vsp.endpointinsightsapi.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.vsp.endpointinsightsapi.dto.DashboardTestActivityDTO.Status.PASS;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@TestPropertySource(properties = "app.authentication.enabled=false")
class DashboardControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    DashboardService dashboardService;

    @Test
    void summary_returnsComputedResponse() throws Exception {

        var input = List.of(
                new DashboardTestActivityDTO(
                        "vision-api-daily",
                        "Vision API",
                        "Daily",
                        LocalDate.parse("2025-07-10"),
                        230,
                        "J. Brock",
                        PASS
                )
        );

        var response = new com.vsp.endpointinsightsapi.dto.DashboardSummaryResponseDTO(
                1, 1, 0, 1.0, 230.0,
                Map.of(PASS, 1L),
                input
        );

        when(dashboardService.calculate(input)).thenReturn(response);

        mvc.perform(post("/api/dashboard/summary")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRuns").value(1))
                .andExpect(jsonPath("$.passedRuns").value(1))
                .andExpect(jsonPath("$.failedRuns").value(0))
                .andExpect(jsonPath("$.passRate").value(1.0))
                .andExpect(jsonPath("$.avgDurationMs").value(230.0));
    }
}
