package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import com.vsp.endpointinsightsapi.model.entity.TestResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.repository.TestBatchEmailListsRepository;
import com.vsp.endpointinsightsapi.repository.TestResultRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final TestBatchEmailListsRepository emailListsRepository;
    private final EmailSender emailSender;
    private final TestResultRepository testResultRepository;

    public NotificationService(TestBatchEmailListsRepository emailListsRepository, EmailSender emailSender, TestResultRepository testResultRepository) {
        this.emailListsRepository = emailListsRepository;
        this.emailSender = emailSender;
        this.testResultRepository = testResultRepository;
    }

    @Transactional
    public void sendTestCompletionNotifications(String batchName, UUID batchId, TestRun testRun, UUID resultId) {
        List<TestBatchEmailList> recipients = emailListsRepository.findAllByBatchId(batchId);
        List<TestResult> testResults = testResultRepository.findByRunId(testRun.getRunId()).orElse(null);
        // use test run to get job id from perf results
        LOG.info("Sending test completion notifications for run {} to {} recipients", testRun.getRunId(), recipients.size());

        for (TestBatchEmailList entry : recipients) {
            String email = entry.getEmail();
            try {
                emailSender.sendTestCompletionEmail(batchName, testRun, email, testResults);
                LOG.info("Notification sent for run {} to {}", testRun.getRunId(), email);
            } catch (Exception ex) {
                LOG.error("Failed to send notification for run {} to {}", testRun.getRunId(), email, ex);
            }
        }
    }
}