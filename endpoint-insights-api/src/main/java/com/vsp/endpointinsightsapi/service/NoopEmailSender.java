package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * No-op implementation of EmailSender for use in tests.
 * Not registered as a Spring bean — see JavaMailEmailSender for the active implementation.
 */
@Service
@Profile("EMAIL-NOOP")
public class NoopEmailSender implements EmailSender {

    private static final Logger LOG = LoggerFactory.getLogger(NoopEmailSender.class);

    @Override
    public void sendTestCompletionEmail(String batchName, TestRun testRun, String recipientEmail, List<TestResult> results) {
        LOG.info("Email would be sent to={} for run={} with result={}", recipientEmail, testRun.getRunId(), results.getFirst().getId());
    }
}
