package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import com.vsp.endpointinsightsapi.repository.TestBatchEmailListsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final TestBatchEmailListsRepository emailListsRepository;

    public NotificationService(TestBatchEmailListsRepository emailListsRepository) {
        this.emailListsRepository = emailListsRepository;
    }

    public void sendTestCompletionNotifications(UUID batchId, UUID runId, UUID resultId) {

        List<TestBatchEmailList> recipients = emailListsRepository.findByBatchId(batchId);

        LOG.info("Sending test completion notifications for run {} to {} recipients", runId, recipients.size());

        for (TestBatchEmailList entry : recipients) {
            String email = entry.getEmail();

            try {
                // TODO: integrate real email sender later
                LOG.info("Notification sent for run {} to {}", runId, email);

            } catch (Exception ex) {
                LOG.error("Failed to send notification for run {} to {}", runId, email, ex);
            }
        }
    }
}