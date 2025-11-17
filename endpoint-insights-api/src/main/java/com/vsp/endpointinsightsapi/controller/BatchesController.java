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

@RestController
@RequestMapping("/api/batches")
public class BatchesController {
	private final static Logger LOG = LoggerFactory.getLogger(BatchesController.class);
	private final BatchService batchService;

	public BatchesController(BatchService batchService) {
		this.batchService = batchService;
	}

	@GetMapping
	public ResponseEntity<List<BatchResponseDTO>> listBatches() {
		List<BatchResponseDTO> batches = List.of(
				new BatchResponseDTO(1L, "Daily API Tests", "ACTIVE"),
				new BatchResponseDTO(2L, "Weekly Regression", "INACTIVE")
		);
		return ResponseEntity.ok(batches);
	}

	@GetMapping("/{id}")
	public ResponseEntity<BatchResponseDTO> getBatch(@PathVariable Long id) {
		BatchResponseDTO batch = new BatchResponseDTO(id, "Example Batch " + id, "ACTIVE");
		return ResponseEntity.ok(batch);
	}

	@PostMapping
	public ResponseEntity<TestBatch> createBatch(@RequestBody BatchRequestDTO request) {
		TestBatch batch = batchService.createBatch(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(batch);
	}

	@PutMapping("/{id}")
	public ResponseEntity<BatchResponseDTO> updateBatch(@PathVariable Long id, @RequestBody BatchRequestDTO request) {
		BatchResponseDTO updated = new BatchResponseDTO(id, request.getName(), "UPDATED");
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBatch(@PathVariable Long id) {
		return ResponseEntity.noContent().build();
	}
}
