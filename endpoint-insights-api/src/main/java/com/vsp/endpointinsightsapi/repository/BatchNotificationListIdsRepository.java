package com.vsp.endpointinsightsapi.repository;


import com.vsp.endpointinsightsapi.model.BatchNotificationListUserId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BatchNotificationListIdsRepository extends JpaRepository<BatchNotificationListUserId, UUID> {
    List<BatchNotificationListUserId> findAllByBatchId(UUID batchId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BatchNotificationListUserId s WHERE s.batchId = :batchId AND s.userId IN :userIds")
    void deleteByBatchIdAndUserIdIn(@Param("batchId") UUID batchId, @Param("userIds") List<UUID> userIds);


    List<BatchNotificationListUserId> deleteAllByBatchId(UUID batchId);
}
