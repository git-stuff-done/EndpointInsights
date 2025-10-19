package com.vsp.endpointinsightsapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
public class BatchesController {

	// GET /api/batches
	@GetMapping
	public List<BatchResponseDTO> listBatches() {
		return List.of(
				new BatchResponseDTO(1L, "Daily API Tests", "ACTIVE"),
				new BatchResponseDTO(2L, "Weekly Regression", "INACTIVE")
		);
	}
	
	// GET /api/batches/{id}
	@GetMapping("/{id}")
	public BatchResponseDTO getBatch(@PathVariable Long id) {
		return new BatchResponseDTO(id, "Example Batch " + id, "ACTIVE");
	}

	// POST /api/batches
	@PostMapping
	public BatchResponseDTO createBatch(@RequestBody BatchRequestDTO request) {
		return new BatchResponseDTO(99L, request.getName(), "CREATED");
	}

	// PUT /api/batches/{id}
	@PutMapping("/{id}")
	public BatchResponseDTO updateBatch(@PathVariable Long id, @RequestBody BatchRequestDTO request) {
		return new BatchResponseDTO(id, request.getName(), "UPDATED");
	}

	// DELETE /api/batches/{id}
	@DeleteMapping("/{id}")
	@ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
	public void deleteBatch(@PathVariable Long id) {
		// stub
	}
}
