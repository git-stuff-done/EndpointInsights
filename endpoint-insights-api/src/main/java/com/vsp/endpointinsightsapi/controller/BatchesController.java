package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.service.BatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches")
public class BatchesController {

    private final BatchService batchService;

    public BatchesController(BatchService batchService){
        this.batchService = batchService;
    }

	@GetMapping
	public ResponseEntity<List<BatchResponseDTO>> listBatches() {
        List<BatchResponseDTO> batches = List.of(
                BatchResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .batchName("Daily API Tests")
                        .scheduleId(334523453L)
                        .startTime(LocalDate.now().minusDays(1))
                        .lastTimeRun(LocalDate.now())
                        .active(true)
//                        .jobs(Collections.emptyList())
                        .build(),
                BatchResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .batchName("Weekly Regression")
                        .scheduleId(42L)
                        .startTime(LocalDate.now().minusWeeks(1))
                        .lastTimeRun(LocalDate.now().minusDays(3))
                        .active(false)
//                        .jobs(Collections.emptyList())
                        .build()
        );
        return ResponseEntity.ok(batches);
	}

	@GetMapping("/{id}")
	public ResponseEntity<BatchResponseDTO> getBatch(@PathVariable UUID id) {
        BatchResponseDTO batch = batchService.getBatchById(id);
        return ResponseEntity.ok(batch);
	}

	@PostMapping
	public ResponseEntity<BatchResponseDTO> createBatch(@RequestBody BatchRequestDTO request) {
        BatchResponseDTO created = BatchResponseDTO.builder()
                .id(UUID.randomUUID())
                .batchName(request.getName())
                .scheduleId(request.getScheduleId())
                .startTime(LocalDate.now())
                .lastTimeRun(null)
                .active(true)
//                .jobs(Collections.emptyList())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	public ResponseEntity<BatchResponseDTO> updateBatch(@PathVariable UUID id, @RequestBody BatchRequestDTO request) {
        BatchResponseDTO updated = BatchResponseDTO.builder()
                .id(id)
                .batchName(request.getName())
                .scheduleId(request.getScheduleId())
                .startTime(request.getStartTime())
                .lastTimeRun(LocalDate.now())
                .active(request.getActive())
//                .jobs(Collections.emptyList())
                .build();

        return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBatch(@PathVariable UUID id) {
		return ResponseEntity.noContent().build();
	}
}
