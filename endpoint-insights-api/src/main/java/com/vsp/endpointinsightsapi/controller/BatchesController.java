package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.CustomExceptionBuilder;
import com.vsp.endpointinsightsapi.mapper.BatchMapper;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.BatchUpdateRequest;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
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
	private final TestBatchRepository testBatchRepository;
	private final BatchMapper batchMapper;

	public BatchesController(BatchService batchService, TestBatchRepository testBatchRepository, BatchMapper batchMapper){
        this.batchService = batchService;
    	this.testBatchRepository = testBatchRepository;
    	this.batchMapper = batchMapper;
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
	public ResponseEntity<BatchResponseDTO> createBatch(@RequestBody BatchRequestDTO request) {
		TestBatch batch = batchService.createBatch(request);
		BatchResponseDTO dto = batchMapper.toDto(batch);
		return ResponseEntity.status(HttpStatus.CREATED).body(dto);
	}

	@PostMapping("/{batchId}/run")
	public ResponseEntity<TestRun> runBatch(@PathVariable UUID batchId) {
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
    public ResponseEntity<BatchResponseDTO> updateBatch(@PathVariable @NotNull UUID id, @RequestBody BatchUpdateRequest request) {
        TestBatch batch = batchService.updateBatch(id, request);
        BatchResponseDTO dto = batchMapper.toDto(batch);
        return ResponseEntity.ok(dto);
    }

	// DELETE /api/batches/{id}
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBatch(@PathVariable UUID id) {
		batchService.deleteBatchById(id);
        return ResponseEntity.noContent().build();
	}

}
