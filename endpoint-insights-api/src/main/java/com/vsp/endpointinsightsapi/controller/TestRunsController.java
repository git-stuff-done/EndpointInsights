package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.RecentActivityDTO;
import com.vsp.endpointinsightsapi.exception.CustomException;
import com.vsp.endpointinsightsapi.exception.CustomExceptionBuilder;
import com.vsp.endpointinsightsapi.model.TestRunCreateRequest;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.service.TestRunService;
import com.vsp.endpointinsightsapi.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/test-runs")
@Tag(name = "Test Runs", description = "Test execution, results, and activity tracking endpoints")
public class TestRunsController {

	private static final Logger LOG = LoggerFactory.getLogger(TestRunsController.class);

	private final TestRunService testRunService;

	public TestRunsController(TestRunService testRunService) {
		this.testRunService = testRunService;
	}

	@GetMapping("/recent")
	@Operation(summary = "Get recent test runs", description = "Retrieves the most recent test run executions")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Recent test runs retrieved"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<List<TestRun>> getRecentTestRuns(
			@Parameter(description = "Number of recent runs to retrieve", example = "10")
			@RequestParam(name = "limit", defaultValue = "10") int limit) {
		return ResponseEntity.ok(testRunService.getRecentTestRuns(limit));
	}

	@GetMapping("/recent-activity")
	@Operation(summary = "Get recent activity", description = "Retrieves recent test activity, optionally filtered by job or batch ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Recent activity retrieved"),
			@ApiResponse(responseCode = "400", description = "Invalid parameters - cannot provide both jobId and batchId"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<List<RecentActivityDTO>> getRecentActivity(
            @Parameter(description = "Filter by job ID")
            @RequestParam(required = false) UUID jobId,
            @Parameter(description = "Filter by batch ID")
            @RequestParam(required = false) UUID batchId,
			@Parameter(description = "Number of activities to retrieve", example = "10")
			@RequestParam(name = "limit", defaultValue = "10") int limit) {
        if (jobId != null && batchId != null) {
            throw new IllegalArgumentException("Provide only one of jobId or batchId");
        }

        List<RecentActivityDTO> result;

        if (jobId == null && batchId == null) {
            // fallback → existing behavior
            result = testRunService.getRecentActivity(limit);
        } else {
            result = testRunService.getRecentActivityById(jobId, batchId, limit);
        }

        return ResponseEntity.ok(result);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get test run by ID", description = "Retrieves a specific test run by its unique identifier")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Test run found"),
			@ApiResponse(responseCode = "404", description = "Test run not found"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<TestRun> getTestRunById(
			@Parameter(description = "Test run ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
			@PathVariable("id") UUID runId) {
		return ResponseEntity.ok(testRunService.getTestRunById(runId));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete test run", description = "Permanently deletes a test run by its ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Test run deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Test run not found"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})

	public ResponseEntity<Map<String, Object>> deleteTestRun(
      @Parameter(description = "Test run ID", required = true)
      @PathVariable("id") UUID runId) {
		return testRunService.deleteTestRunById(runId);
	}

	@DeleteMapping
  @Operation(summary = "Delete test runs before a specific date", description = "Permanently deletes all test runs that were finished before the specified purge date")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Test runs deleted successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid purge date - cannot be in the future"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<Map<String, Object>> deleteBefore(@RequestParam("purgeDate") Instant purgeDate) {
		if (purgeDate.isAfter(Instant.now())) {
			throw new CustomExceptionBuilder(HttpStatus.BAD_REQUEST, "purgeDate cannot be in the future").build();
		}

//		return ResponseEntity.ok(Map.of("status", "Test runs deleted successfully"));
		return testRunService.deleteBefore(purgeDate);
	}
}
