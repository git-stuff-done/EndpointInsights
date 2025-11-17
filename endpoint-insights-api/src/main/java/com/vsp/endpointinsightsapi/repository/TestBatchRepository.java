package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.TestBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface TestBatchRepository extends JpaRepository<TestBatch, UUID> {
    @Query("SELECT t FROM TestBatch t where t.batch_id = :id")
    Optional<TestBatch> findById(@Param("id")UUID id);
}
