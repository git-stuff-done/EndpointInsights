package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.service.BatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches")
public class BatchesController {

	private static final Logger LOG = LoggerFactory.getLogger(BatchesController.class);

	private final BatchService batchService;

	public BatchesController(BatchService batchService) {
		this.batchService = batchService;
	}

	// GET /api/batches — stub list (unchanged)
	@GetMapping
	public ResponseEntity<List<BatchResponseDTO>> listBatches() {
		List<BatchResponseDTO> batches = List.of(
				new BatchResponseDTO(1L, "Daily API Tests", "ACTIVE"),
				new BatchResponseDTO(2L, "Weekly Regression", "INACTIVE")
		);
		return ResponseEntity.ok(batches);
	}

	// GET /api/batches/{id}
	@GetMapping("/{id}")
	public ResponseEntity<TestBatch> getBatch(@PathVariable UUID id) {
		return batchService.getBatchById(id)
				.map(b -> ResponseEntity.ok(b))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	// POST /api/batches
	@PostMapping
	public ResponseEntity<TestBatch> createBatch(@RequestBody BatchRequestDTO request) {
		TestBatch batch = batchService.createBatch(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(batch);
	}

	// PUT /api/batches/{id} — stubbed
	@PutMapping("/{id}")
	public ResponseEntity<BatchResponseDTO> updateBatch(@PathVariable Long id,
														@RequestBody BatchRequestDTO request) {
		BatchResponseDTO updated = new BatchResponseDTO(id, request.getName(), "UPDATED");
		return ResponseEntity.ok(updated);
	}

	// DELETE /api/batches/{id} — stubbed
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBatch(@PathVariable Long id) {
		return ResponseEntity.noContent().build();
	}
}
