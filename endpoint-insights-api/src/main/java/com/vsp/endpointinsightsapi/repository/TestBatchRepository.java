package com.vsp.endpointinsightsapi.repository;
import com.vsp.endpointinsightsapi.model.TestBatch;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestBatchRepository extends JpaRepository<TestBatch, UUID> {
}
