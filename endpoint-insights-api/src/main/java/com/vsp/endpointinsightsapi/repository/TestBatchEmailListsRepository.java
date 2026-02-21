package com.vsp.endpointinsightsapi.repository;


import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestBatchEmailListsRepository extends JpaRepository<TestBatchEmailList, Integer> {

    @Transactional
    List<TestBatchEmailList> findAllByBatchId(UUID batchId);

    Optional<TestBatchEmailList> findByEmail(String email);

    List<TestBatchEmailList> findByBatchIdAndEmail(UUID batchId, String email);

    List<TestBatchEmailList> findByBatchIdAndEmailOrderByEmailAsc(UUID batchId, String email);

    @Transactional
    @Modifying
    void deleteAllByBatchId(UUID batchId);
}
