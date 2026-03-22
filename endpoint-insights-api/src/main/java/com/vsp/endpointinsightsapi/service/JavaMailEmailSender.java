package com.vsp.endpointinsightsapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Profile("!EMAIL-NOOP")
public class JavaMailEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    public JavaMailEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendTestCompletionEmail(UUID runId, UUID resultId, String recipientEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipientEmail);
        message.setSubject("Test Run Completed - " + runId);
        message.setText(buildEmailBody(runId, resultId));
        mailSender.send(message);
    }

    private String buildEmailBody(UUID runId, UUID resultId) {
        String status = resultId != null ? "COMPLETED" : "FAILED";
        String resultLine = resultId != null
                ? "Result ID: " + resultId
                : "No result data available (test may have failed to execute).";
        return "Your test run has " + status + ".\n\nRun ID: " + runId + "\n" + resultLine;
    }
}
