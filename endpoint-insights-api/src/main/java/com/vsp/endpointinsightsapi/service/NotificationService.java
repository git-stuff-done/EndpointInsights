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
    private final EmailSender emailSender;

    public NotificationService(TestBatchEmailListsRepository emailListsRepository, EmailSender emailSender) {
        this.emailListsRepository = emailListsRepository;
        this.emailSender = emailSender;
    }

    public void sendTestCompletionNotifications(UUID batchId, UUID runId, UUID resultId) {
        List<TestBatchEmailList> recipients = emailListsRepository.findAllByBatchId(batchId);

        LOG.info("Sending test completion notifications for run {} to {} recipients", runId, recipients.size());

        for (TestBatchEmailList entry : recipients) {
            String email = entry.getEmail();
            try {
                emailSender.sendTestCompletionEmail(runId, resultId, email);
                LOG.info("Notification sent for run {} to {}", runId, email);
            } catch (Exception ex) {
                LOG.error("Failed to send notification for run {} to {}", runId, email, ex);
            }
        }
    }
}