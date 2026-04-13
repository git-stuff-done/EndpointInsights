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
    private NotificationGroupService notificationGroupService;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        emailListsRepository = mock(TestBatchEmailListsRepository.class);
        emailSender = mock(EmailSender.class);
        notificationGroupService = mock(NotificationGroupService.class);
        testResultRepository = mock(TestResultRepository.class);
        notificationService = new NotificationService(emailListsRepository, emailSender, notificationGroupService, testResultRepository);
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

    @Test
    void sendTestCompletionNotifications_resolveGroupsToEmails() throws MessagingException, IOException {
        UUID batchId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        TestRun testRun = new TestRun();
        testRun.setRunId(runId);
        TestResult testResult = new TestResult();
        testResult.setId(resultId);

        TestBatchEmailList groupEntry = new TestBatchEmailList(null, batchId, null, "group:" + groupId);

        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of(groupEntry));
        when(notificationGroupService.getGroupMemberEmails(groupId)).thenReturn(List.of("group@test.com"));
        when(testResultRepository.findByRunId(runId)).thenReturn(Optional.of(List.of(testResult)));

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, resultId);

        verify(notificationGroupService, times(1)).getGroupMemberEmails(groupId);
        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("group@test.com"), eq(List.of(testResult)));
    }

    @Test
    void sendTestCompletionNotifications_mixedEmailsAndGroups() throws MessagingException, IOException {
        UUID batchId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        TestRun testRun = new TestRun();
        testRun.setRunId(runId);
        TestResult testResult = new TestResult();
        testResult.setId(resultId);

        TestBatchEmailList emailEntry = new TestBatchEmailList(null, batchId, null, "direct@test.com");
        TestBatchEmailList groupEntry = new TestBatchEmailList(null, batchId, null, "group:" + groupId);

        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of(emailEntry, groupEntry));
        when(notificationGroupService.getGroupMemberEmails(groupId)).thenReturn(List.of("group@test.com"));
        when(testResultRepository.findByRunId(runId)).thenReturn(Optional.of(List.of(testResult)));

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, resultId);

        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("direct@test.com"), eq(List.of(testResult)));
        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("group@test.com"), eq(List.of(testResult)));
    }

    @Test
    void sendTestCompletionNotifications_invalidGroupIdFormat_logsWarning() throws MessagingException, IOException {
        UUID batchId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        TestRun testRun = new TestRun();
        testRun.setRunId(runId);

        TestBatchEmailList invalidGroupEntry = new TestBatchEmailList(null, batchId, null, "group:invalid-uuid");

        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of(invalidGroupEntry));
        when(testResultRepository.findByRunId(runId)).thenReturn(Optional.of(List.of()));

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, resultId);

        verify(notificationGroupService, never()).getGroupMemberEmails(any());
        verifyNoInteractions(emailSender);
    }

    @Test
    void sendTestCompletionNotifications_deduplicatesEmails() throws MessagingException, IOException {
        UUID batchId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        TestRun testRun = new TestRun();
        testRun.setRunId(runId);
        TestResult testResult = new TestResult();
        testResult.setId(resultId);

        TestBatchEmailList a = new TestBatchEmailList(null, batchId, null, "shared@test.com");
        TestBatchEmailList b = new TestBatchEmailList(null, batchId, null, "shared@test.com");

        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of(a, b));
        when(testResultRepository.findByRunId(runId)).thenReturn(Optional.of(List.of(testResult)));

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, resultId);

        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("shared@test.com"), eq(List.of(testResult)));
    }
}