package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.service.BatchService;
import com.vsp.endpointinsightsapi.model.TestBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
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

    // GET /api/batches — stub list (unchanged)
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

	// PUT /api/batches/{id} — stubbed
	@PutMapping("/update")
	public ResponseEntity<BatchResponseDTO> updateBatch(@RequestBody BatchRequestDTO request) {
        BatchResponseDTO result = batchService.updateBatch(request);
        return ResponseEntity.ok(result);
	}

	// DELETE /api/batches/{id}
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBatch(@PathVariable UUID id) {
		batchService.deleteBatchById(id);
        return ResponseEntity.noContent().build();
	}

    @PutMapping("/remove-participants")
    public ResponseEntity<List<UUID>> removeParticipants(@RequestParam("userIds") List<UUID> userIds,
                                                   @RequestParam("batchId") UUID batchId) {
        List<UUID> res = batchService.deleteParticipants(userIds, batchId);
        return ResponseEntity.ok(res);
    }
}
