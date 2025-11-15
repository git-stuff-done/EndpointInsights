package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.service.BatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches")
public class BatchesController {

	private final BatchService batchService;

	public BatchesController(BatchService batchService) {
		this.batchService = batchService;
	}

	//api/batches — List all batches
	@GetMapping
	public ResponseEntity<List<BatchResponseDTO>> listBatches() {
		List<BatchResponseDTO> batches = List.of(
				new BatchResponseDTO(1L, "Daily API Tests", "ACTIVE"),
				new BatchResponseDTO(2L, "Weekly Regression", "INACTIVE")
		);
		return ResponseEntity.ok(batches);
	}

	//GET /api/batches/{id} — Retrieve a single batch
	@GetMapping("/{id}")
	public ResponseEntity<TestBatch> getBatch(@PathVariable UUID id) {
		return batchService.getBatchById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	//POST /api/batches — Create a new batch (stubbed)\
	@PostMapping
	public ResponseEntity<BatchResponseDTO> createBatch(@RequestBody BatchRequestDTO request) {
		BatchResponseDTO created = new BatchResponseDTO(99L, request.getName(), "CREATED");
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	//PUT /api/batches/{id}
	@PutMapping("/{id}")
	public ResponseEntity<BatchResponseDTO> updateBatch(@PathVariable Long id, @RequestBody BatchRequestDTO request) {
		BatchResponseDTO updated = new BatchResponseDTO(id, request.getName(), "UPDATED");
		return ResponseEntity.ok(updated);
	}

	//DELETE /api/batches/{id}
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBatch(@PathVariable Long id) {
		return ResponseEntity.noContent().build();
	}
}
