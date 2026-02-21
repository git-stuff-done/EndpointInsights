package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByIdempotencyKey(String idempotencyKey);
}