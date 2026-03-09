package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.TestRunCreateRequest;
import com.vsp.endpointinsightsapi.service.TestRunService;
import com.vsp.endpointinsightsapi.exception.CustomException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-runs")
public class TestRunsController {

	private static final Logger LOG = LoggerFactory.getLogger(TestRunsController.class);

	private final TestRunService testRunService;

	public TestRunsController(TestRunService testRunService) {
		this.testRunService = testRunService;
	}

	@GetMapping("/recent")
	public ResponseEntity<List<TestRun>> getRecentTestRuns(
			@RequestParam(name = "limit", defaultValue = "10") int limit) {
		return ResponseEntity.ok(testRunService.getRecentTestRuns(limit));
	}

	@PostMapping
	public ResponseEntity<TestRun> createTestRun(@RequestBody @Valid TestRunCreateRequest request) {
		try {
			TestRun testRun = new TestRun();
			testRun.setJobId(request.getJobId());
            testRun.setBatchId(request.getBatchId());
			testRun.setRunBy(request.getRunBy());
			testRun.setStatus(request.getStatus());
			testRun.setStartedAt(request.getStartedAt());
			testRun.setFinishedAt(request.getFinishedAt());

			TestRun saved = testRunService.createTestRun(testRun);
			return new ResponseEntity<>(saved, HttpStatus.CREATED);
		} catch (CustomException e) {
			throw e;
		} catch (RuntimeException e) {
			LOG.error("Error creating test run: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
}
