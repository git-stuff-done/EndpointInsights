package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface EmailSender {
    void sendTestCompletionEmail(String batchName, TestRun testRun, String recipientEmail, List<TestResult> results) throws MessagingException, IOException;
}