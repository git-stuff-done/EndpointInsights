package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.service.TestRunService;
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
	public ResponseEntity<TestRun> createTestRun(@RequestBody @Valid TestRun testRun) {
		try {
			TestRun saved = testRunService.createTestRun(testRun);
			return new ResponseEntity<>(saved, HttpStatus.CREATED);
		} catch (RuntimeException e) {
			LOG.error("Error creating test run: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
}
