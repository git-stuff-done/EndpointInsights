package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.TestBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestBatchRepository extends JpaRepository<TestBatch, UUID> {

    @Query("""
    SELECT DISTINCT t FROM TestBatch t
            LEFT JOIN FETCH t.creator
            LEFT JOIN FETCH t.updater
            LEFT JOIN FETCH t.jobs j
            LEFT JOIN FETCH j.creator
            LEFT JOIN FETCH j.updater
            WHERE COALESCE(:batchName, t.batchName) = t.batchName
            AND (CAST(:lastRunDate AS TIMESTAMP) IS NULL OR t.lastTimeRun = :lastRunDate)
    """)
    List<TestBatch> findAllByCriteria(@Param("batchName") String batchName,
                                      @Param("lastRunDate") LocalDateTime lastRunDate);

    @Query("""
    SELECT DISTINCT t FROM TestBatch t
            LEFT JOIN FETCH t.creator
            LEFT JOIN FETCH t.updater
            LEFT JOIN FETCH t.jobs j
            LEFT JOIN FETCH j.creator
            LEFT JOIN FETCH j.updater
            WHERE t.batchId = :batchId
    """)
    Optional<TestBatch> findByIdWithJobsAndUsers(@Param("batchId") UUID batchId);

}