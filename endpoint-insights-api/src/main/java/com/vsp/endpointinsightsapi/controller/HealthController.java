package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.authentication.PublicAPI;
import com.vsp.endpointinsightsapi.authentication.RequiredRoles;
import com.vsp.endpointinsightsapi.model.enums.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @PublicAPI
    @GetMapping("/api/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @RequiredRoles(roles = {UserRole.READ, UserRole.WRITE})
    @GetMapping("/api/health-secure")
    public ResponseEntity<String> healthCheckSecure() {
        return ResponseEntity.ok("OK");
    }

}
