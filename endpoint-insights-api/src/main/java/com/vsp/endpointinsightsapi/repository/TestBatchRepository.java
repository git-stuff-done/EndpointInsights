package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.TestBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TestBatchRepository extends JpaRepository<TestBatch, UUID> {

    @Query("""
    SELECT t FROM TestBatch t
            WHERE COALESCE(:batchName, t.batchName) = t.batchName
            AND (CAST(:lastRunDate AS TIMESTAMP) IS NULL OR t.lastTimeRun = :lastRunDate)
    """)
    List<TestBatch> findAllByCriteria(@Param("batchName") String batchName,
                                      @Param("lastRunDate") LocalDateTime lastRunDate);




}