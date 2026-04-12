package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;

import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.BatchUpdateRequest;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;

import com.vsp.endpointinsightsapi.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches")
@Tag(name = "Batches", description = "Test batch management and execution endpoints")
public class BatchesController {

    private static final Logger LOG = LoggerFactory.getLogger(BatchesController.class);

    private final BatchService batchService;
	private final TestBatchRepository testBatchRepository;

	public BatchesController(BatchService batchService, TestBatchRepository testBatchRepository){
        this.batchService = batchService;
    	this.testBatchRepository = testBatchRepository;
	}

    // GET /api/batches
	@GetMapping
	@Operation(summary = "List all test batches", description = "Retrieves a list of all test batches with optional filtering")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Batches retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<List<BatchResponseDTO>> listBatches(
			@Parameter(description = "Filter by batch name", example = "Smoke Tests")
			@RequestParam(required = false) String batchName,
			@Parameter(description = "Filter by run date (ISO format)")
			@RequestParam(required = false) LocalDateTime runDate) {
        List<BatchResponseDTO> dto = batchService.getAllBatchesByCriteria(batchName, runDate);
        return ResponseEntity.ok(dto);
	}

	// GET /api/batches/{id}
	@GetMapping("/{id}")
	@Operation(summary = "Get batch by ID", description = "Retrieves a specific test batch by its unique identifier")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Batch found"),
			@ApiResponse(responseCode = "404", description = "Batch not found"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<BatchResponseDTO> getBatch(
			@Parameter(description = "Batch ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
			@PathVariable UUID id) {
        BatchResponseDTO batch = batchService.getBatchById(id);
        return ResponseEntity.ok(batch);
	}

	// POST /api/batches
	@PostMapping
	@Operation(summary = "Create new test batch", description = "Creates a new test batch with the provided configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Batch created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<TestBatch> createBatch(@RequestBody BatchRequestDTO request) {
		TestBatch batch = batchService.createBatch(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(batch);
	}

	@PostMapping("/{batchId}/run")
	@Operation(summary = "Run test batch", description = "Executes a test batch and creates a new test run")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Batch execution started"),
			@ApiResponse(responseCode = "404", description = "Batch not found"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<TestRun> runBatch(
			@Parameter(description = "Batch ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
			@PathVariable UUID batchId) {
		LOG.info("Request received to run batch {}", batchId);
		var batchOptional = testBatchRepository.findById(batchId);
		if (batchOptional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		var batch = batchOptional.get();

		return ResponseEntity.ok(batchService.runBatch(batch));
	}

	// PUT /api/batches/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Update test batch", description = "Updates an existing test batch with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Batch updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Batch not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<TestBatch> updateBatch(
            @Parameter(description = "Batch ID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody BatchUpdateRequest request) {
        TestBatch batch = batchService.updateBatch(id, request);
        return ResponseEntity.ok(batch);
    }

	// DELETE /api/batches/{id}
	@DeleteMapping("/{id}")
	@Operation(summary = "Delete test batch", description = "Permanently deletes a test batch by its ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Batch deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Batch not found"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<Void> deleteBatch(
			@Parameter(description = "Batch ID", required = true)
			@PathVariable UUID id) {
		batchService.deleteBatchById(id);
        return ResponseEntity.noContent().build();
	}

}
