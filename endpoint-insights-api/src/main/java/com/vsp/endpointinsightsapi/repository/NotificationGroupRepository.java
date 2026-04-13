package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.NotificationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationGroupRepository extends JpaRepository<NotificationGroup, UUID> {
    Optional<NotificationGroup> findByName(String name);
}
