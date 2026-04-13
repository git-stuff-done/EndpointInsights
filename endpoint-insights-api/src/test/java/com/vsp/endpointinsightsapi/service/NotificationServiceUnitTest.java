package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import com.vsp.endpointinsightsapi.model.entity.TestResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.repository.TestBatchEmailListsRepository;
import com.vsp.endpointinsightsapi.repository.TestResultRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationServiceUnitTest {

    private TestBatchEmailListsRepository emailListsRepository;
    private EmailSender emailSender;
    private NotificationService notificationService;
    private TestResultRepository testResultRepository;

    @BeforeEach
    void setUp() {
        emailListsRepository = mock(TestBatchEmailListsRepository.class);
        emailSender = mock(EmailSender.class);
        testResultRepository = mock(TestResultRepository.class);
        notificationService = new NotificationService(emailListsRepository, emailSender, testResultRepository);
    }

    @Test
    void sendTestCompletionNotifications_sendsToAllRecipients() throws MessagingException, IOException {
        UUID batchId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        TestRun testRun = new TestRun();
        testRun.setRunId(runId);
        TestResult testResult = new TestResult();
        testResult.setId(resultId);

        TestBatchEmailList a = new TestBatchEmailList(null, batchId, null, "a@test.com");
        TestBatchEmailList b = new TestBatchEmailList(null, batchId, null, "b@test.com");

        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of(a, b));
        when(testResultRepository.findByRunId(runId)).thenReturn(Optional.of(List.of(testResult)));

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, resultId);

        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("a@test.com"), eq(List.of(testResult)));
        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("b@test.com"), eq(List.of(testResult)));
        verifyNoMoreInteractions(emailSender);
    }

    @Test
    void sendTestCompletionNotifications_noRecipients_doesNotSend() {
        UUID batchId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        TestRun testRun = new TestRun();
        testRun.setRunId(runId);

        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of());
        when(testResultRepository.findByRunId(runId)).thenReturn(Optional.of(List.of()));

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, resultId);

        verify(emailListsRepository, times(1)).findAllByBatchId(eq(batchId));
        verifyNoInteractions(emailSender);
    }

    @Test
    void sendTestCompletionNotifications_senderThrows_continuesSending() throws MessagingException, IOException {
        UUID batchId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        TestResult testResult = new TestResult();
        testResult.setId(resultId);
        TestRun testRun = new TestRun();
        testRun.setRunId(runId);

        TestBatchEmailList a = new TestBatchEmailList(null, batchId, null, "a@test.com");
        TestBatchEmailList b = new TestBatchEmailList(null, batchId, null, "b@test.com");

        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of(a, b));
        when(testResultRepository.findByRunId(runId)).thenReturn(Optional.of(List.of(testResult)));

        doThrow(new RuntimeException("boom"))
                .when(emailSender).sendTestCompletionEmail(eq("test"), eq(testRun), eq("a@test.com"), eq(List.of(testResult)));

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, resultId);

        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("a@test.com"), eq(List.of(testResult)));
        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("b@test.com"), eq(List.of(testResult)));
    }
}