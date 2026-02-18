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
import com.vsp.endpointinsightsapi.model.BatchNotificationListUserId;
import com.vsp.endpointinsightsapi.model.BatchUpdateRequest;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.BatchNotificationListIdsRepository;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchService {

    private static final Logger LOG = LoggerFactory.getLogger(BatchService.class);
    private final TestBatchRepository testBatchRepository;
    private final BatchMapper batchMapper;
    private final JobRepository jobRepository;
    private final BatchNotificationListIdsRepository batchNotificationListIdsRepository;


    //TODO fill with search criteria when filter implemented, change parameters as well
    public List<BatchResponseDTO> getAllBatchesByCriteria(String batchName, LocalDateTime runDate) {
        List<TestBatch> res = testBatchRepository.findAllByCriteria(batchName, runDate);

        //Find all notifications for this batch object and assign
        for (TestBatch testBatch : res) {
            List<UUID> notificationUserIds = batchNotificationListIdsRepository
                    .findAllByBatchId(testBatch.getBatch_id())
                    .stream()
                    .map(BatchNotificationListUserId::getUserId)
                    .toList();
            testBatch.setNotificationList(notificationUserIds);
        }
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

    public List<UUID> deleteParticipants(List<UUID> userIds, UUID batchId) {
        batchNotificationListIdsRepository.deleteByBatchIdAndUserIdIn(batchId, userIds);
        List<BatchNotificationListUserId> r = batchNotificationListIdsRepository.findAllByBatchId(batchId);
        return r.stream().map(BatchNotificationListUserId::getUserId).toList();
    }

//    //TODO hook this up to work under one method
//    @Transactional
//    public BatchResponseDTO updateBatch(BatchRequestDTO batchRequestDTO) {
//        TestBatch existing = testBatchRepository.findById(batchRequestDTO.getId())
//                .orElseThrow(() -> new BatchNotFoundException(batchRequestDTO.getId().toString()));
//
//        existing.setBatch_id(batchRequestDTO.getId());
//        existing.setBatchName(batchRequestDTO.getBatchName());
//        existing.setScheduleId(batchRequestDTO.getScheduleId());
//        existing.setLastTimeRun(batchRequestDTO.getLastTimeRun());
//        existing.setNotificationList(batchRequestDTO.getNotificationList());
//        existing.setJobs(batchRequestDTO.getJobs());
//        existing.setActive(batchRequestDTO.getActive());
//        TestBatch saved = testBatchRepository.save(existing);
//
//        batchNotificationListIdsRepository.deleteAllByBatchId(batchRequestDTO.getId());
//
//        batchRequestDTO.getNotificationList().forEach(userId -> {
//            BatchNotificationListUserId batchNotificationListUserId = new BatchNotificationListUserId();
//            batchNotificationListUserId.setBatchId(batchRequestDTO.getId());
//            batchNotificationListUserId.setUserId(userId);
//            batchNotificationListIdsRepository.save(batchNotificationListUserId);
//        });
//        return batchMapper.toDto(saved);
//    }

    //Create Batch — used by POST /api/batches
    public TestBatch createBatch(BatchRequestDTO request) {
        TestBatch batch = new TestBatch();
        batch.setBatchName(request.getBatchName());

        if (request.getJobs() != null && !request.getJobs().isEmpty()) {
            List<Job> jobs = jobRepository.findAllById(request.getJobs().stream().map(Job::getJobId).toList());
            if (jobs.size() != request.getJobs().size()) {
                LOG.warn("Job ID(s) in the request do not exist");
            }
            batch.setJobs(jobs);
        }
        return testBatchRepository.save(batch);
    }

    public TestBatch updateBatch(UUID id, BatchUpdateRequest request) {
		Optional<TestBatch> batchOptional = testBatchRepository.findById(id);
        if (batchOptional.isEmpty()) {
            throw new CustomExceptionBuilder(HttpStatus.NOT_FOUND, "Batch does not exist with id=" + id).build();
        }

		TestBatch batch = batchOptional.get();

        if (request.cronExpression != null) {
            batch.setCronExpression(request.cronExpression);
        }

        // handle adding jobs
        if (request.addJobs != null && !request.addJobs.isEmpty()) {
            Map<UUID, Job> jobMap = jobRepository.findAllById(request.addJobs).stream().collect(Collectors.toMap(Job::getJobId, job -> job));
            for (UUID jobId : request.addJobs) {
				Optional<Job> job = jobMap.containsKey(jobId) ? Optional.of(jobMap.get(jobId)) : Optional.empty();
                if (job.isEmpty()) {
                    throw new CustomExceptionBuilder(HttpStatus.BAD_REQUEST, "Job does not exist with id=" + jobId).build();
                }
                if (batch.getJobs().contains(job.get())) {
                    throw new CustomExceptionBuilder(HttpStatus.BAD_REQUEST, "Job already exists in batch with id=" + id).build();
                }
                batch.getJobs().add(job.get());
            }
        }

        // handle deleting jobs
        if (request.deleteJobs != null && !request.deleteJobs.isEmpty()) {
            Map<UUID, Job> jobMap = jobRepository.findAllById(request.deleteJobs).stream().collect(Collectors.toMap(Job::getJobId, job -> job));
            for (UUID jobId : request.deleteJobs) {
                Optional<Job> job = jobMap.containsKey(jobId) ? Optional.of(jobMap.get(jobId)) : Optional.empty();
                if (job.isEmpty()) {
                    throw new CustomExceptionBuilder(HttpStatus.BAD_REQUEST, "Job does not exist with id=" + jobId).build();
                }
                if (!batch.getJobs().contains(job.get())) {
                    throw new CustomExceptionBuilder(HttpStatus.BAD_REQUEST, "Job does not exist in batch with id=" + id).build();
                }
                batch.getJobs().remove(job.get());
            }
        }

        testBatchRepository.save(batch);
        return batch;
    }
}
