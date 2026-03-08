package com.vsp.endpointinsightsapi.runner;

import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestRunResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.model.enums.TestType;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import com.vsp.endpointinsightsapi.service.NotificationService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JobRunnerThreadTest {

    @Mock
    private TestRunRepository testRunRepository;

    @Mock
    private JMeterInterpreterService jMeterInterpreterService;

    @Mock
    private NotificationService notificationService;

    private Job job;
    private TestRun testRun;

    @BeforeEach
    void setUp() {
        job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setName("Test Job");
        job.setJobType(TestType.PERF);

        testRun = new TestRun();
        testRun.setRunId(UUID.randomUUID());
        testRun.setJobId(job.getJobId());

        when(testRunRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // --- Constructor ---

    @Test
    void constructor_perfJobType_usesJMeterInterpreter() {
        job.setJobType(TestType.PERF);
        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);

        Object interpreter = ReflectionTestUtils.getField(thread, "testInterpreter");
        assertSame(jMeterInterpreterService, interpreter);
    }

    @Test
    void constructor_integrationJobType_setsNullInterpreter() {
        job.setJobType(TestType.INTEGRATION);
        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);

        Object interpreter = ReflectionTestUtils.getField(thread, "testInterpreter");
        assertNull(interpreter);
    }

    @Test
    void constructor_e2eJobType_setsNullInterpreter() {
        job.setJobType(TestType.E2E);
        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);

        Object interpreter = ReflectionTestUtils.getField(thread, "testInterpreter");
        assertNull(interpreter);
    }

    // --- run() status transitions ---

    @Test
    void run_noRunCommand_setsFailedStatus() {
        job.setGitUrl(null);
        job.setRunCommand(null);

        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);
        thread.run();

        assertEquals(TestRunStatus.FAILED, testRun.getStatus());
        assertNotNull(testRun.getFinishedAt());
    }

    @Test
    void run_noRunCommand_savesRunningThenFailed() {
        job.setGitUrl(null);
        job.setRunCommand(null);

        List<TestRunStatus> savedStatuses = new ArrayList<>();
        doAnswer(inv -> {
            savedStatuses.add(testRun.getStatus());
            return testRun;
        }).when(testRunRepository).save(testRun);

        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);
        thread.run();

        assertEquals(List.of(TestRunStatus.RUNNING, TestRunStatus.FAILED), savedStatuses);
    }

    @Test
    void run_emptyRunCommand_setsFailedStatus() {
        job.setGitUrl(null);
        job.setRunCommand("   ");

        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);
        thread.run();

        assertEquals(TestRunStatus.FAILED, testRun.getStatus());
        verify(testRunRepository, times(2)).save(testRun);
    }

    @Test
    void run_nonExistentCommand_setsFailedStatus() {
        job.setGitUrl(null);
        job.setCompileCommand(null);
        job.setRunCommand("this-command-does-not-exist-xyz123");

        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);
        thread.run();

        assertEquals(TestRunStatus.FAILED, testRun.getStatus());
        assertNotNull(testRun.getFinishedAt());
    }

    // --- run() notifications ---

    @Test
    void run_withBatchId_sendsNotification() {
        UUID batchId = UUID.randomUUID();
        testRun.setBatchId(batchId);
        job.setGitUrl(null);
        job.setRunCommand(null);

        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);
        thread.run();

        verify(notificationService).sendTestCompletionNotifications(
                eq(batchId), eq(testRun.getRunId()), any());
    }

    @Test
    void run_withNullBatchId_doesNotSendNotification() {
        testRun.setBatchId(null);
        job.setGitUrl(null);
        job.setRunCommand(null);

        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);
        thread.run();

        verifyNoInteractions(notificationService);
    }

    @Test
    void run_nonExistentCommand_withBatchId_sendsNotification() {
        UUID batchId = UUID.randomUUID();
        testRun.setBatchId(batchId);
        job.setGitUrl(null);
        job.setCompileCommand(null);
        job.setRunCommand("this-command-does-not-exist-xyz123");

        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);
        thread.run();

        verify(notificationService).sendTestCompletionNotifications(
                eq(batchId), eq(testRun.getRunId()), any());
    }

    // --- run() with processResults ---

    @Test
    void run_successfulProcess_callsProcessResultsAndSetsCompleted() throws Exception {
        job.setJobType(TestType.PERF);
        job.setGitUrl(null);
        job.setCompileCommand(null);
        // Use a real executable that exists on all Unix systems and succeeds
        Assumptions.assumeTrue(
                !System.getProperty("os.name", "").toLowerCase().contains("win"),
                "Skipped on Windows: test uses Unix 'true' command"
        );
        job.setRunCommand("true");

        UUID resultId = UUID.randomUUID();
        when(jMeterInterpreterService.processResults(any(File.class)))
                .thenReturn(new TestRunResult(true, resultId));

        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);
        thread.run();

        assertEquals(TestRunStatus.COMPLETED, testRun.getStatus());
        assertEquals(resultId, testRun.getResultId());
        assertNotNull(testRun.getFinishedAt());
        verify(jMeterInterpreterService).processResults(any(File.class));
    }

    @Test
    void run_failedProcessResults_setsFailedStatus() throws Exception {
        job.setJobType(TestType.PERF);
        job.setGitUrl(null);
        job.setCompileCommand(null);
        Assumptions.assumeTrue(
                !System.getProperty("os.name", "").toLowerCase().contains("win"),
                "Skipped on Windows: test uses Unix 'true' command"
        );
        job.setRunCommand("true");

        when(jMeterInterpreterService.processResults(any(File.class)))
                .thenReturn(new TestRunResult(false, UUID.randomUUID()));

        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);
        thread.run();

        assertEquals(TestRunStatus.FAILED, testRun.getStatus());
        assertNotNull(testRun.getFinishedAt());
    }

    // --- enhanceRunCommand() ---

    @Test
    void enhanceRunCommand_nonJmeterCommand_splitsByWhitespace() {
        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);

        String[] result = (String[]) ReflectionTestUtils.invokeMethod(thread, "enhanceRunCommand", "npm run test");

        assertArrayEquals(new String[]{"npm", "run", "test"}, result);
    }

    @Test
    void enhanceRunCommand_singleToken_returnsSingleElement() {
        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);

        String[] result = (String[]) ReflectionTestUtils.invokeMethod(thread, "enhanceRunCommand", "mvn");

        assertArrayEquals(new String[]{"mvn"}, result);
    }

    @Test
    void enhanceRunCommand_jmeterCommand_throwsWhenJmeterHomeNotSet() {
        Assumptions.assumeTrue(
                System.getenv("JMETER_HOME") == null || System.getenv("JMETER_HOME").trim().isEmpty(),
                "Skipped: JMETER_HOME is set in this environment"
        );

        JobRunnerThread thread = new JobRunnerThread(job, testRun, testRunRepository, jMeterInterpreterService, notificationService);

        assertThrows(IllegalStateException.class, () ->
                ReflectionTestUtils.invokeMethod(thread, "enhanceRunCommand", "jmeter -n -t test.jmx")
        );
    }
}
