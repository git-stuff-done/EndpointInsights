package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.Notification;
import com.vsp.endpointinsightsapi.model.enums.NotificationChannel;
import com.vsp.endpointinsightsapi.model.enums.NotificationStatus;
import com.vsp.endpointinsightsapi.model.enums.NotificationType;
import com.vsp.endpointinsightsapi.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void recordTestCompletionEmail(
            UUID runId,
            UUID batchId,
            UUID resultId,
            String recipientEmail,
            boolean success,
            String providerMessageId,
            String error
    ) {
        String idempotencyKey = buildIdempotencyKey(runId, NotificationType.TEST_COMPLETED, NotificationChannel.EMAIL, recipientEmail);

        Notification n = new Notification();
        n.setRunId(runId);
        n.setBatchId(batchId);
        n.setResultId(resultId);
        n.setNotificationType(NotificationType.TEST_COMPLETED);
        n.setChannel(NotificationChannel.EMAIL);
        n.setRecipient(recipientEmail);
        n.setStatus(success ? NotificationStatus.SENT : NotificationStatus.FAILED);
        n.setProviderMessageId(providerMessageId);
        n.setError(error);
        n.setIdempotencyKey(idempotencyKey);

        try {
            notificationRepository.save(n);
            LOG.debug("Recorded notification {} for run {} recipient {}", n.getStatus(), runId, recipientEmail);
        } catch (DataIntegrityViolationException ex) {
            // Unique constraint on idempotency_key
            LOG.debug("Duplicate notification record ignored for run {} recipient {}", runId, recipientEmail);
        }
    }

    private static String buildIdempotencyKey(UUID runId, NotificationType type, NotificationChannel channel, String recipient) {
        return runId + "|" + type + "|" + channel + "|" + recipient.trim().toLowerCase(Locale.ROOT);
    }
}