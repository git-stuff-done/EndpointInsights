package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.model.entity.BatchUpdateRequest;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import com.vsp.endpointinsightsapi.runner.JobRunnerThread;
import com.vsp.endpointinsightsapi.service.BatchService;
import com.vsp.endpointinsightsapi.util.CurrentUser;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches")
public class BatchesController {

    private static final Logger LOG = LoggerFactory.getLogger(BatchesController.class);

    private final BatchService batchService;
	private final TestBatchRepository testBatchRepository;
	private final TestRunRepository testRunRepository;

	public BatchesController(BatchService batchService, TestBatchRepository testBatchRepository, TestRunRepository testRunRepository){
        this.batchService = batchService;
    	this.testBatchRepository = testBatchRepository;
		this.testRunRepository = testRunRepository;
	}

    // GET /api/batches
	@GetMapping
	public ResponseEntity<List<BatchResponseDTO>> listBatches(@RequestParam(required = false) String batchName,
                                                              @RequestParam(required = false) LocalDateTime runDate) {
        List<BatchResponseDTO> dto = batchService.getAllBatchesByCriteria(batchName, runDate);
        return ResponseEntity.ok(dto);
	}

	// GET /api/batches/{id}
	@GetMapping("/{id}")
	public ResponseEntity<BatchResponseDTO> getBatch(@PathVariable UUID id) {
        BatchResponseDTO batch = batchService.getBatchById(id);
        return ResponseEntity.ok(batch);
	}

	// POST /api/batches
	@PostMapping
	public ResponseEntity<TestBatch> createBatch(@RequestBody BatchRequestDTO request) {
		TestBatch batch = batchService.createBatch(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(batch);
	}

	@PostMapping("/{jobId}/run")
	public ResponseEntity<TestRun> runBatch(@PathVariable UUID jobId) {
		LOG.info("Running job {}", jobId);
		var batch = testBatchRepository.findById(jobId);
		if (batch.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		TestRun testRun = new TestRun();
		testRun.setStartedAt(Instant.now());
		testRun.setStatus(TestRunStatus.PENDING);
		testRun.setBatchId(batch.get().getBatchId());
		testRun.setRunBy(CurrentUser.getUserId());
		testRun = testRunRepository.save(testRun);

		//todo: batch runner thread to spawn job runner threads?

		return ResponseEntity.ok(testRun);
	}

	// PUT /api/batches/{id}
    @PutMapping("/{id}")
    public ResponseEntity<TestBatch> updateBatch(@PathVariable @NotNull UUID id, @RequestBody BatchUpdateRequest request) {
        TestBatch batch = batchService.updateBatch(id, request);
        return ResponseEntity.ok(batch);
    }

	// DELETE /api/batches/{id}
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBatch(@PathVariable UUID id) {
		batchService.deleteBatchById(id);
        return ResponseEntity.noContent().build();
	}

}
