package com.vsp.endpointinsightsapi.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NoopEmailSender implements EmailSender {
    @Override
    public void sendTestCompletionEmail(UUID runId, UUID resultId, String recipientEmail) {
        // TODO integrate real email sending later
    }
}