package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.DashboardSummaryResponseDTO;
import com.vsp.endpointinsightsapi.dto.DashboardTestActivityDTO;
import com.vsp.endpointinsightsapi.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @PostMapping("/summary")
    public ResponseEntity<DashboardSummaryResponseDTO> summary(@RequestBody List<DashboardTestActivityDTO> tests) {
        return ResponseEntity.ok(service.calculate(tests));
    }
}