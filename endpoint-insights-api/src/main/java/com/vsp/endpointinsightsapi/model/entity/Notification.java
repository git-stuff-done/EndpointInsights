package com.vsp.endpointinsightsapi.model.entity;

import com.vsp.endpointinsightsapi.model.enums.NotificationChannel;
import com.vsp.endpointinsightsapi.model.enums.NotificationStatus;
import com.vsp.endpointinsightsapi.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "notifications",
        uniqueConstraints = @UniqueConstraint(name = "notifications_idempotency_uq", columnNames = "idempotency_key")
)
public class Notification {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "notification_id")
    private UUID notificationId;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "result_id")
    private UUID resultId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "error")
    private String error;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (sentAt == null) sentAt = now;
    }
}