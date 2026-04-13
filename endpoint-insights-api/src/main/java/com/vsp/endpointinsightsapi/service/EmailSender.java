package com.vsp.endpointinsightsapi.service;

import java.util.UUID;

public interface EmailSender {
    void sendTestCompletionEmail(UUID runId, UUID resultId, String recipientEmail);
}