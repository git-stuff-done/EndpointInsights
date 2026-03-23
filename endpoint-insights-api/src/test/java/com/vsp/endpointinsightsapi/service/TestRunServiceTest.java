package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.RecentActivityDTO;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.exception.TestRunNotFoundException;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestRunServiceTest {

	@Mock
	private TestRunRepository testRunRepository;

	@Mock
	private JobRepository jobRepository;

	@Mock
	private TestBatchRepository testBatchRepository;

	@InjectMocks
	private TestRunService testRunService;

	@Test
	void createTestRun_returnsSavedRun() {
		TestRun run = new TestRun();
		UUID jobId = UUID.randomUUID();
		run.setRunId(UUID.randomUUID());
		run.setJobId(jobId);
		run.setRunBy("tester");
		run.setStatus(TestRunStatus.COMPLETED);

		when(jobRepository.existsById(jobId)).thenReturn(true);
		when(testRunRepository.save(run)).thenReturn(run);

		TestRun saved = testRunService.createTestRun(run);
		assertNotNull(saved);
		verify(jobRepository).existsById(jobId);
		verify(testRunRepository).save(run);
	}

	@Test
	void createTestRun_missingJob_throwsException() {
		TestRun run = new TestRun();
		UUID jobId = UUID.randomUUID();
		run.setJobId(jobId);
		run.setRunBy("tester");
		run.setStatus(TestRunStatus.COMPLETED);

		when(jobRepository.existsById(jobId)).thenReturn(false);

		assertThrows(JobNotFoundException.class, () -> testRunService.createTestRun(run));
		verify(jobRepository).existsById(jobId);
	}

	@Test
	void createTestRun_nullJobId_throwsException() {
		TestRun run = new TestRun();
		run.setJobId(null);
		run.setRunBy("tester");
		run.setStatus(TestRunStatus.COMPLETED);

		assertThrows(JobNotFoundException.class, () -> testRunService.createTestRun(run));
	}

	@Test
	void getRecentTestRuns_limitsResults() {
		TestRun run = new TestRun();
		run.setRunId(UUID.randomUUID());
		run.setJobId(UUID.randomUUID());
		run.setRunBy("tester");
		run.setStatus(TestRunStatus.COMPLETED);
		run.setFinishedAt(Instant.now());

		when(testRunRepository.findAllByOrderByFinishedAtDesc(any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(run)));

		List<TestRun> result = testRunService.getRecentTestRuns(1);
		assertEquals(1, result.size());

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(testRunRepository).findAllByOrderByFinishedAtDesc(captor.capture());
		assertEquals(1, captor.getValue().getPageSize());
	}

	// ── getRecentActivity tests ─────────────────────────────────────────────

	private TestRun buildRun(UUID jobId, UUID batchId, TestRunStatus status) {
		TestRun run = new TestRun();
		run.setRunId(UUID.randomUUID());
		run.setJobId(jobId);
		run.setBatchId(batchId);
		run.setRunBy("tester");
		run.setStatus(status);
		run.setStartedAt(Instant.now());
		run.setFinishedAt(Instant.now().plusMillis(3250));
		return run;
	}

	@Test
	void getRecentActivity_mapsJobNameCorrectly() {
		UUID jobId = UUID.randomUUID();
		Job job = new Job();
		job.setJobId(jobId);
		job.setName("Vision API");

		when(testRunRepository.findAllByOrderByFinishedAtDesc(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(buildRun(jobId, null, TestRunStatus.COMPLETED))));
		when(jobRepository.findAllById(any())).thenReturn(List.of(job));
		when(testBatchRepository.findAllById(any())).thenReturn(List.of());

		assertEquals("Vision API", testRunService.getRecentActivity(1).get(0).getTestName());
	}

	@Test
	void getRecentActivity_naWhenNoBatch() {
		UUID jobId = UUID.randomUUID();

		when(testRunRepository.findAllByOrderByFinishedAtDesc(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(buildRun(jobId, null, TestRunStatus.COMPLETED))));
		when(jobRepository.findAllById(any())).thenReturn(List.of());
		when(testBatchRepository.findAllById(any())).thenReturn(List.of());

		assertEquals("N/A", testRunService.getRecentActivity(1).get(0).getGroup());
	}

	@Test
	void getRecentActivity_dailyWhenCronRunsEveryDay() {
		UUID jobId = UUID.randomUUID();
		UUID batchId = UUID.randomUUID();
		TestBatch batch = new TestBatch();
		batch.setBatchId(batchId);
		batch.setCronExpression("0 0 8 * * *");

		when(testRunRepository.findAllByOrderByFinishedAtDesc(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(buildRun(jobId, batchId, TestRunStatus.COMPLETED))));
		when(jobRepository.findAllById(any())).thenReturn(List.of());
		when(testBatchRepository.findAllById(any())).thenReturn(List.of(batch));

		assertEquals("Daily", testRunService.getRecentActivity(1).get(0).getGroup());
	}

	@Test
	void getRecentActivity_mapsCompletedToPass() {
		UUID jobId = UUID.randomUUID();

		when(testRunRepository.findAllByOrderByFinishedAtDesc(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(buildRun(jobId, null, TestRunStatus.COMPLETED))));
		when(jobRepository.findAllById(any())).thenReturn(List.of());
		when(testBatchRepository.findAllById(any())).thenReturn(List.of());

		assertEquals("PASS", testRunService.getRecentActivity(1).get(0).getStatus());
	}

	@Test
	void getRecentActivity_mapsJobIdToString() {
		UUID jobId = UUID.randomUUID();

		when(testRunRepository.findAllByOrderByFinishedAtDesc(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(buildRun(jobId, null, TestRunStatus.COMPLETED))));
		when(jobRepository.findAllById(any())).thenReturn(List.of());
		when(testBatchRepository.findAllById(any())).thenReturn(List.of());

		assertEquals(jobId.toString(), testRunService.getRecentActivity(1).get(0).getJobId());
	}

	@Test
	void getRecentActivity_nullJobId_mapsToNull() {
		TestRun run = new TestRun();
		run.setRunId(UUID.randomUUID());
		run.setJobId(null);
		run.setRunBy("tester");
		run.setStatus(TestRunStatus.COMPLETED);
		run.setStartedAt(Instant.now());
		run.setFinishedAt(Instant.now());

		when(testRunRepository.findAllByOrderByFinishedAtDesc(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(run)));
		when(jobRepository.findAllById(any())).thenReturn(List.of());
		when(testBatchRepository.findAllById(any())).thenReturn(List.of());

		assertNull(testRunService.getRecentActivity(1).get(0).getJobId());
	}

	@Test
	void getRecentTestRuns_capsLimitAt100() {
		when(testRunRepository.findAllByOrderByFinishedAtDesc(any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of()));

		testRunService.getRecentTestRuns(500);

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(testRunRepository).findAllByOrderByFinishedAtDesc(captor.capture());
		assertEquals(100, captor.getValue().getPageSize());
	}

	@Test
	void getRecentTestRuns_raisesLimitToMinimumOf1() {
		when(testRunRepository.findAllByOrderByFinishedAtDesc(any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of()));

		testRunService.getRecentTestRuns(0);

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(testRunRepository).findAllByOrderByFinishedAtDesc(captor.capture());
		assertEquals(1, captor.getValue().getPageSize());
	}

	@Test
	void getTestRunById_returnsRun() {
		UUID runId = UUID.randomUUID();
		TestRun run = new TestRun();
		run.setRunId(runId);

		when(testRunRepository.findById(runId)).thenReturn(Optional.of(run));

		TestRun result = testRunService.getTestRunById(runId);
		assertEquals(runId, result.getRunId());
		verify(testRunRepository).findById(runId);
	}

	@Test
	void getTestRunById_missingRun_throwsException() {
		UUID runId = UUID.randomUUID();
		when(testRunRepository.findById(runId)).thenReturn(Optional.empty());

		assertThrows(TestRunNotFoundException.class, () -> testRunService.getTestRunById(runId));
		verify(testRunRepository).findById(runId);
	}

	@Test
	void deleteTestRunById_existingRun_deletesRun() {
		UUID runId = UUID.randomUUID();
		when(testRunRepository.existsById(runId)).thenReturn(true);

		testRunService.deleteTestRunById(runId);

		verify(testRunRepository).existsById(runId);
		verify(testRunRepository).deleteById(runId);
	}

	@Test
	void deleteTestRunById_missingRun_throwsException() {
		UUID runId = UUID.randomUUID();
		when(testRunRepository.existsById(runId)).thenReturn(false);

		assertThrows(TestRunNotFoundException.class, () -> testRunService.deleteTestRunById(runId));
		verify(testRunRepository).existsById(runId);
	}

    @Test
    void getRecentActivityById_withNoParams_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> testRunService.getRecentActivityById(null, null, 10));
    }

    @Test
    void getRecentActivityById_withJobId_mapsCorrectly() {
        UUID jobId = UUID.randomUUID();
        UUID batchId = UUID.randomUUID();

        Job job = new Job();
        job.setJobId(jobId);
        job.setName("Endpoint Smoke");

        TestBatch batch = new TestBatch();
        batch.setBatchId(batchId);
        batch.setCronExpression("0 0 8 * * *");

        TestRun run = buildRun(jobId, batchId, TestRunStatus.COMPLETED);

        when(testRunRepository.findByJobIdOrderByFinishedAtDesc(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(run)));
        when(jobRepository.findAllById(any())).thenReturn(List.of(job));
        when(testBatchRepository.findAllById(any())).thenReturn(List.of(batch));

        RecentActivityDTO result =
                testRunService.getRecentActivityById(jobId, null, 10).get(0);

        assertEquals("Endpoint Smoke", result.getTestName());
        assertEquals("Daily", result.getGroup());
        assertEquals("PASS", result.getStatus());
    }

    @Test
    void getRecentActivityById_withBatchId_mapsCorrectly() {
        UUID jobId = UUID.randomUUID();
        UUID batchId = UUID.randomUUID();

        Job job = new Job();
        job.setJobId(jobId);
        job.setName("Batch Job");

        TestBatch batch = new TestBatch();
        batch.setBatchId(batchId);
        batch.setCronExpression("0 0 8 * * *");

        TestRun run = buildRun(jobId, batchId, TestRunStatus.COMPLETED);

        when(testRunRepository.findByBatchIdOrderByFinishedAtDesc(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(run)));
        when(jobRepository.findAllById(any())).thenReturn(List.of(job));
        when(testBatchRepository.findAllById(any())).thenReturn(List.of(batch));

        RecentActivityDTO result =
                testRunService.getRecentActivityById(null, batchId, 10).get(0);

        assertEquals("Batch Job", result.getTestName());
        assertEquals("Daily", result.getGroup());
    }

    @Test
    void getRecentActivityById_capsLimitAt100_withJobId() {
        UUID jobId = UUID.randomUUID();

        when(testRunRepository.findByJobIdOrderByFinishedAtDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        testRunService.getRecentActivityById(jobId, null, 1000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(testRunRepository).findByJobIdOrderByFinishedAtDesc(any(), captor.capture());

        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    void getRecentActivityById_capsLimitAt100_withBatchId() {
        UUID batchId = UUID.randomUUID();

        when(testRunRepository.findByBatchIdOrderByFinishedAtDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        testRunService.getRecentActivityById(null, batchId, 1000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(testRunRepository).findByBatchIdOrderByFinishedAtDesc(any(), captor.capture());

        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    void getRecentActivity_mapsBatchIdToString() {
        UUID batchId = UUID.randomUUID();

        TestRun run = new TestRun();
        run.setRunId(UUID.randomUUID());
        run.setJobId(null);
        run.setBatchId(batchId);
        run.setRunBy("tester");
        run.setStatus(TestRunStatus.COMPLETED);
        run.setStartedAt(Instant.now());
        run.setFinishedAt(Instant.now().plusMillis(1000));

        when(testRunRepository.findAllByOrderByFinishedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(run)));
        when(jobRepository.findAllById(any())).thenReturn(List.of());
        when(testBatchRepository.findAllById(any())).thenReturn(List.of());

        assertEquals(batchId.toString(), testRunService.getRecentActivity(1).get(0).getBatchId());
    }

    @Test
    void getRecentActivityById_withBatchId_mapsBatchIdToString() {
        UUID jobId = UUID.randomUUID();
        UUID batchId = UUID.randomUUID();

        Job job = new Job();
        job.setJobId(jobId);
        job.setName("Batch Job");

        TestBatch batch = new TestBatch();
        batch.setBatchId(batchId);
        batch.setCronExpression("0 0 8 * * *");

        TestRun run = buildRun(jobId, batchId, TestRunStatus.COMPLETED);

        when(testRunRepository.findByBatchIdOrderByFinishedAtDesc(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(run)));
        when(jobRepository.findAllById(any())).thenReturn(List.of(job));
        when(testBatchRepository.findAllById(any())).thenReturn(List.of(batch));

        RecentActivityDTO result = testRunService.getRecentActivityById(null, batchId, 10).get(0);

        assertEquals(batchId.toString(), result.getBatchId());
    }
}
