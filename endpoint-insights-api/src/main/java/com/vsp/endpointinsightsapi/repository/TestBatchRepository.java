package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.TestBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TestBatchRepository extends JpaRepository<TestBatch, Long> {

    @Query("SELECT t FROM TestBatch t where t.batch_id = :id")
    TestBatch findById(@Param("id")UUID id);
}
