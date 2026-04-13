package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import com.vsp.endpointinsightsapi.repository.TestBatchEmailListsRepository;
import com.vsp.endpointinsightsapi.repository.TestResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);
    private static final String GROUP_IDENTIFIER_PREFIX = "group:";

    private final TestBatchEmailListsRepository emailListsRepository;
    private final EmailSender emailSender;
    private final NotificationGroupService notificationGroupService;
    private final TestResultRepository testResultRepository;

    public NotificationService(TestBatchEmailListsRepository emailListsRepository,
                              EmailSender emailSender,
                              NotificationGroupService notificationGroupService,
                              TestResultRepository testResultRepository) {
        this.emailListsRepository = emailListsRepository;
        this.emailSender = emailSender;
        this.notificationGroupService = notificationGroupService;
        this.testResultRepository = testResultRepository;
    }

    public void sendTestCompletionNotifications(UUID batchId, UUID runId, UUID resultId) {
        List<TestBatchEmailList> recipients = emailListsRepository.findAllByBatchId(batchId);

        LOG.info("Sending test completion notifications for run {} to {} recipients", runId, recipients.size());

        // Resolve emails and groups to unique email addresses
        Set<String> allEmails = new HashSet<>();

        for (TestBatchEmailList entry : recipients) {
            String emailOrGroup = entry.getEmail();

            // Check if this is a group identifier
            if (emailOrGroup.startsWith(GROUP_IDENTIFIER_PREFIX)) {
                String groupIdStr = emailOrGroup.substring(GROUP_IDENTIFIER_PREFIX.length());
                try {
                    UUID groupId = UUID.fromString(groupIdStr);
                    List<String> groupEmails = notificationGroupService.getGroupMemberEmails(groupId);
                    allEmails.addAll(groupEmails);
                    LOG.debug("Resolved group {} to {} members", groupId, groupEmails.size());
                } catch (IllegalArgumentException ex) {
                    LOG.warn("Invalid group identifier format: {}", emailOrGroup, ex);
                }
            } else {
                // Regular email address
                allEmails.add(emailOrGroup);
            }
        }

        LOG.info("Resolved to {} unique email recipients for run {}", allEmails.size(), runId);

        // Send emails to all resolved recipients
        for (String email : allEmails) {
            try {
                emailSender.sendTestCompletionEmail(runId, resultId, email);
                LOG.info("Notification sent for run {} to {}", runId, email);
            } catch (Exception ex) {
                LOG.error("Failed to send notification for run {} to {}", runId, email, ex);
            }
        }
    }
}