package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.repository.JobRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
