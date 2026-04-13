package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.entity.NotificationGroupMember;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationGroupMemberRepository extends JpaRepository<NotificationGroupMember, UUID> {

    @Transactional
    List<NotificationGroupMember> findAllByGroupId(UUID groupId);

    Optional<NotificationGroupMember> findByGroupIdAndEmail(UUID groupId, String email);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM NotificationGroupMember m WHERE m.groupId = :groupId")
    void deleteAllByGroupId(@Param("groupId") UUID groupId);
}
