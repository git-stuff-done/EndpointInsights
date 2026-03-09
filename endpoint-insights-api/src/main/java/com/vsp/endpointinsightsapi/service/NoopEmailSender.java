package com.vsp.endpointinsightsapi.service;

import java.util.UUID;

/**
 * No-op implementation of EmailSender for use in tests.
 * Not registered as a Spring bean — see JavaMailEmailSender for the active implementation.
 */
public class NoopEmailSender implements EmailSender {
    @Override
    public void sendTestCompletionEmail(UUID runId, UUID resultId, String recipientEmail) {
        // intentional no-op
    }
}
