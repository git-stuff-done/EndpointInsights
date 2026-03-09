package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.model.entity.BatchUpdateRequest;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import com.vsp.endpointinsightsapi.service.BatchService;
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
public class BatchesController {

    private static final Logger LOG = LoggerFactory.getLogger(BatchesController.class);

    private final BatchService batchService;

    public BatchesController(BatchService batchService){
        this.batchService = batchService;
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

    // GET /api/batches/{id}/emails
    @GetMapping("/{id}/emails")
    public ResponseEntity<List<String>> getEmails(@PathVariable UUID id) {
        List<String> emails =  batchService.getEmailsForBatch(id);
        return ResponseEntity.ok(emails);
    }

    // PUT /api/batches/{id}/emails
    @PutMapping("/{id}/emails")
    public ResponseEntity<List<String>> updateEmails(@PathVariable UUID id, @RequestBody List<String> emails) {
        batchService.updateEmailsForBatch(id, emails);
        List<String> updatedEmails =  batchService.getEmailsForBatch(id);
        return ResponseEntity.ok(updatedEmails);
    }

}
