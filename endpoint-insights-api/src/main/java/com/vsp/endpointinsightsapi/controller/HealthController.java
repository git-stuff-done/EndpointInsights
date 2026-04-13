package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.authentication.PublicAPI;
import com.vsp.endpointinsightsapi.authentication.RequiredRoles;
import com.vsp.endpointinsightsapi.model.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Health check and system status endpoints")
public class HealthController {

    @PublicAPI
    @GetMapping("/api/health")
    @Operation(summary = "Health check endpoint", description = "Returns the health status of the API. No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API is healthy",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @RequiredRoles(roles = {UserRole.READ, UserRole.WRITE})
    @GetMapping("/api/health-secure")
    @Operation(summary = "Secure health check endpoint", description = "Returns the health status of the API. Requires READ or WRITE role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API is healthy",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<String> healthCheckSecure() {
        return ResponseEntity.ok("OK");
    }

}
