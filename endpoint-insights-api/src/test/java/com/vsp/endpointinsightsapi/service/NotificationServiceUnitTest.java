package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import com.vsp.endpointinsightsapi.repository.TestBatchEmailListsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationServiceUnitTest {

    private TestBatchEmailListsRepository emailListsRepository;
    private EmailSender emailSender;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        emailListsRepository = mock(TestBatchEmailListsRepository.class);
        emailSender = mock(EmailSender.class);
        notificationService = new NotificationService(emailListsRepository, emailSender);
    }

    @Test
    void sendTestCompletionNotifications_sendsToAllRecipients() {
        UUID batchId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();

        TestBatchEmailList a = new TestBatchEmailList(null, batchId, null, "a@test.com");
        TestBatchEmailList b = new TestBatchEmailList(null, batchId, null, "b@test.com");

        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of(a, b));

        notificationService.sendTestCompletionNotifications(batchId, runId, resultId);

        verify(emailListsRepository, times(1)).findAllByBatchId(eq(batchId));
        verify(emailSender, times(1)).sendTestCompletionEmail(eq(runId), eq(resultId), eq("a@test.com"));
        verify(emailSender, times(1)).sendTestCompletionEmail(eq(runId), eq(resultId), eq("b@test.com"));
        verifyNoMoreInteractions(emailSender);
    }

    @Test
    void sendTestCompletionNotifications_noRecipients_doesNotSend() {
        UUID batchId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();

        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of());

        notificationService.sendTestCompletionNotifications(batchId, runId, resultId);

        verify(emailListsRepository, times(1)).findAllByBatchId(eq(batchId));
        verifyNoInteractions(emailSender);
    }

    @Test
    void sendTestCompletionNotifications_senderThrows_continuesSending() {
        UUID batchId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();

        TestBatchEmailList a = new TestBatchEmailList(null, batchId, null, "a@test.com");
        TestBatchEmailList b = new TestBatchEmailList(null, batchId, null, "b@test.com");

        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of(a, b));

        doThrow(new RuntimeException("boom"))
                .when(emailSender).sendTestCompletionEmail(eq(runId), eq(resultId), eq("a@test.com"));

        notificationService.sendTestCompletionNotifications(batchId, runId, resultId);

        verify(emailSender, times(1)).sendTestCompletionEmail(eq(runId), eq(resultId), eq("a@test.com"));
        verify(emailSender, times(1)).sendTestCompletionEmail(eq(runId), eq(resultId), eq("b@test.com"));
    }
}