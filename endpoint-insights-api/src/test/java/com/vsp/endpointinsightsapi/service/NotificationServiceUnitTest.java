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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationServiceUnitTest {

    private TestBatchEmailListsRepository emailListsRepository;
    private EmailSender emailSender;
    private NotificationGroupService notificationGroupService;
    private TestResultRepository testResultRepository;
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
    void sendTestCompletionNotifications_sendsToAllRecipients() throws MessagingException, IOException, MessagingException {
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

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, List.of(testResult));
        verify(emailListsRepository, times(1)).findAllByBatchId(eq(batchId));
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
        TestResult testResult = new TestResult();
        testResult.setId(resultId);
        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of());

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, List.of(testResult));
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

        doThrow(new RuntimeException("boom"))
                .when(emailSender).sendTestCompletionEmail(eq("test"), eq(testRun), eq("a@test.com"), eq(List.of(testResult)));

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, List.of(testResult));
        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("a@test.com"), eq(List.of(testResult)));
        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("b@test.com"), eq(List.of(testResult)));
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

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, List.of(testResult));

        verify(notificationGroupService, times(1)).getGroupMemberEmails(groupId);
        verify(emailSender, times(1)).sendTestCompletionEmail("test", testRun, "group@test.com", List.of(testResult));
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

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, List.of(testResult));
        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("direct@test.com"), eq(List.of(testResult)));
        verify(emailSender, times(1)).sendTestCompletionEmail(eq("test"), eq(testRun), eq("group@test.com"), eq(List.of(testResult)));

    }

    @Test
    void sendTestCompletionNotifications_invalidGroupIdFormat_logsWarning() {
        UUID batchId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        TestRun testRun = new TestRun();
        testRun.setRunId(runId);
        TestResult testResult = new TestResult();
        testResult.setId(resultId);

        TestBatchEmailList invalidGroupEntry = new TestBatchEmailList(null, batchId, null, "group:invalid-uuid");

        when(emailListsRepository.findAllByBatchId(batchId)).thenReturn(List.of(invalidGroupEntry));

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, List.of(testResult));

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

        notificationService.sendTestCompletionNotifications("test", batchId, testRun, List.of(testResult));

        verify(emailSender, times(1)).sendTestCompletionEmail(any(), eq(testRun), eq("shared@test.com"), eq(List.of(testResult)));
    }


}