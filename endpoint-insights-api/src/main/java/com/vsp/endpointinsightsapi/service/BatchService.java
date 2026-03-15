package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.exception.CustomExceptionBuilder;
import com.vsp.endpointinsightsapi.mapper.BatchMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import java.util.*;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.model.entity.BatchUpdateRequest;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchEmailListsRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchService {

    private static final Logger LOG = LoggerFactory.getLogger(BatchService.class);
    private final TestBatchRepository testBatchRepository;
    private final BatchMapper batchMapper;
    private final JobRepository jobRepository;
    private final TestBatchEmailListsRepository testBatchEmailListsRepository;

    //TODO fill with search criteria when filter implemented, change parameters as well
    public List<BatchResponseDTO> getAllBatchesByCriteria(String batchName, LocalDateTime runDate) {
        List<TestBatch> res = testBatchRepository.findAllByCriteria(batchName, runDate);
        return res.stream().map(batchMapper::toDto).toList();
    }

    //Get Batch — used by GET /api/batches/{id}
    public BatchResponseDTO getBatchById(UUID batchId) {
        TestBatch b = testBatchRepository.findById(batchId)
                .orElseThrow(() -> {
                    LOG.debug("Batch {} not found", batchId);
                    return new BatchNotFoundException(batchId.toString());
                });
        return batchMapper.toDto(b);
    }

    //Delete Batch — used by DELETE /api/batches/{id}
    public void deleteBatchById(UUID batchId) {
        if (!testBatchRepository.existsById(batchId)) {
            LOG.debug("Batch {} not found", batchId);
            throw new BatchNotFoundException(batchId.toString());
        }
        testBatchRepository.deleteById(batchId);
        LOG.info("Deleted batch {}", batchId);
    }

    //Create Batch — used by POST /api/batches
    @Transactional
    public TestBatch createBatch(BatchRequestDTO request) {
        TestBatch batch = new TestBatch();
        batch.setBatchName(request.getBatchName());

        if (request.getJobs() != null && !request.getJobs().isEmpty()) {
            List<Job> jobs = jobRepository.findAllById(request.getJobs());
            if (jobs.size() != request.getJobs().size()) {
                LOG.warn("One or more job ID(s) in the request do not exist");
            }
            batch.setJobs(jobs);
        }

        TestBatch saved = testBatchRepository.saveAndFlush(batch);

        if (request.getEmails() != null && !request.getEmails().isEmpty()) {
            updateEmailsForBatch(saved.getBatchId(), request.getEmails());
        }

        return saved;
    }

    //Update Batch — used by PUT /api/batches/{id}
    @Transactional
    public TestBatch updateBatch(UUID id, BatchUpdateRequest request) {
        Optional<TestBatch> batchOptional = testBatchRepository.findById(id);
        if (batchOptional.isEmpty()) {
            throw new CustomExceptionBuilder(HttpStatus.NOT_FOUND, "Batch does not exist with id=" + id).build();
        }

        TestBatch batch = batchOptional.get();

        if (request.getCronExpression() != null) {
            batch.setCronExpression(request.getCronExpression());
        }

        if (request.getBatchName() != null) {
            batch.setBatchName(request.getBatchName());
        }

        if (request.getJobs() != null) {
            List<Job> jobs = jobRepository.findAllById(request.getJobs());
            if (jobs.size() != request.getJobs().size()) {
                throw new CustomExceptionBuilder(HttpStatus.BAD_REQUEST, "One or more job IDs not found").build();
            }
            batch.setJobs(new ArrayList<>(jobs));
        }

        testBatchRepository.save(batch);

        if (request.getEmails() != null) {
            updateEmailsForBatch(id, request.getEmails());
        }

        return batch;
    }

    private List<String> getEmailsForBatch(UUID batchId) {
        return testBatchEmailListsRepository.findAllByBatchId(batchId).stream()
                .map(TestBatchEmailList::getEmail)
                .toList();
    }

    @Transactional
    public void updateEmailsForBatch(UUID batchId, List<String> emails) {
        testBatchEmailListsRepository.deleteAllByBatchId(batchId);
        List<TestBatchEmailList> existingEmails = testBatchEmailListsRepository.findAllByBatchId(batchId);
        System.out.println(existingEmails);
        List<TestBatchEmailList> entities = emails.stream()
                .collect(Collectors.toMap(
                        String::toLowerCase,
                        email -> email,
                        (existing, duplicate) -> existing
                ))
                .values()
                .stream()
                .map(email -> {
                    TestBatchEmailList entity = new TestBatchEmailList();
                    entity.setBatchId(batchId);
                    entity.setEmail(email);
                    return entity;
                })
                .toList();

        testBatchEmailListsRepository.saveAll(entities);
    }
}
