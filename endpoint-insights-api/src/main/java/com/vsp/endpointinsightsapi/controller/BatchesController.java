package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.validation.ErrorMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@Validated
public class BatchesController {

	private static final Logger LOG = LoggerFactory.getLogger(BatchesController.class);

	 //Returns a list of all batches.
	@GetMapping
	public ResponseEntity<List<BatchResponseDTO>> getBatches() {
		LOG.info("Retrieving all batches");
		List<BatchResponseDTO> batches = List.of(
				new BatchResponseDTO(1L, "Daily API Tests", "ACTIVE"),
				new BatchResponseDTO(2L, "Weekly Regression", "INACTIVE")
		);
		return ResponseEntity.ok(batches);
	}

	 // Retrieves a single batch by ID.
	@GetMapping("/{id}")
	public ResponseEntity<BatchResponseDTO> getBatch(
			@PathVariable("id")
			@NotNull(message = ErrorMessages.BATCH_ID_REQUIRED)
			Long id) {
		LOG.info("Retrieving batch with ID {}", id);
		BatchResponseDTO batch = new BatchResponseDTO(id, "Example Batch " + id, "ACTIVE");
		return ResponseEntity.ok(batch);
	}

	 //Creates a new batch.
	@PostMapping
	public ResponseEntity<BatchResponseDTO> createBatch(@RequestBody @Valid BatchRequestDTO request) {
		LOG.info("Creating new batch");
		BatchResponseDTO created = new BatchResponseDTO(99L, request.getName(), "CREATED");
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	 //Updates an existing batch.
	@PutMapping("/{id}")
	public ResponseEntity<BatchResponseDTO> updateBatch(
			@PathVariable("id")
			@NotNull(message = ErrorMessages.BATCH_ID_REQUIRED)
			Long id,
			@RequestBody @Valid BatchRequestDTO request) {
		LOG.info("Updating batch with ID {}", id);
		BatchResponseDTO updated = new BatchResponseDTO(id, request.getName(), "UPDATED");
		return ResponseEntity.ok(updated);
	}

	//Deletes a batch by ID.
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteBatch(
			@PathVariable("id")
			@NotNull(message = ErrorMessages.BATCH_ID_REQUIRED)
			Long id) {
		LOG.info("Deleting batch with ID {}", id);
		return ResponseEntity.ok(String.format("Batch %s deleted", id));
	}
}
