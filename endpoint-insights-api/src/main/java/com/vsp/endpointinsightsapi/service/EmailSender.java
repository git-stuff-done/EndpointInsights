package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.UUID;

public interface EmailSender {
    void sendTestCompletionEmail(String batchName, TestRun testRun, String recipientEmail, TestResult results) throws MessagingException, IOException;
}