package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.RecentActivityDTO;
import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.exception.TestRunNotFoundException;
import com.vsp.endpointinsightsapi.model.TestRunCreateRequest;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.service.TestRunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.authentication.enabled=false")
@WebMvcTest(TestRunsController.class)
@AutoConfigureWebMvc
class TestRunsControllerUnitTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TestRunService testRunService;

	@Test
	void getRecentTestRuns_returnsList() throws Exception {
		TestRun run = new TestRun();
		run.setRunId(UUID.randomUUID());
		run.setJobId(UUID.randomUUID());
		run.setRunBy("tester");
		run.setBatchId(UUID.randomUUID());
		run.setStatus(TestRunStatus.COMPLETED);
		run.setFinishedAt(Instant.now());

		when(testRunService.getRecentTestRuns(5)).thenReturn(List.of(run));

		mockMvc.perform(get("/api/test-runs/recent").param("limit", "5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].runId").value(run.getRunId().toString()))
				.andExpect(jsonPath("$[0].jobId").value(run.getJobId().toString()))
				.andExpect(jsonPath("$[0].runBy").value("tester"))
				.andExpect(jsonPath("$[0].status").value("COMPLETED"));
	}

    @Test
    void getRecentActivity_returnsList() throws Exception {
        RecentActivityDTO activity = RecentActivityDTO.builder()
                .runId(UUID.randomUUID().toString())
                .jobId(UUID.randomUUID().toString())
                .testName("Endpoint_Insight_Health")
                .group("Daily")
                .dateRun(Instant.parse("2025-07-10T10:15:30Z"))
                .durationMs(6469)
                .startedBy("tester")
                .status("PASS")
                .build();

        when(testRunService.getRecentActivity(5)).thenReturn(List.of(activity));

        mockMvc.perform(get("/api/test-runs/recent-activity").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].runId").value(activity.getRunId()))
                .andExpect(jsonPath("$[0].jobId").value(activity.getJobId()))
                .andExpect(jsonPath("$[0].testName").value("Endpoint_Insight_Health"))
                .andExpect(jsonPath("$[0].group").value("Daily"))
                .andExpect(jsonPath("$[0].durationMs").value(6469))
                .andExpect(jsonPath("$[0].startedBy").value("tester"))
                .andExpect(jsonPath("$[0].status").value("PASS"));
    }

    @Test
    void getRecentActivity_withJobId_returnsList() throws Exception {
        UUID jobId = UUID.randomUUID();

        RecentActivityDTO activity = RecentActivityDTO.builder()
                .runId(UUID.randomUUID().toString())
                .jobId(jobId.toString())
                .testName("Endpoint_Insight_Health")
                .group("Daily")
                .dateRun(Instant.parse("2025-07-10T10:15:30Z"))
                .durationMs(6254)
                .startedBy("tester")
                .status("PASS")
                .build();

        when(testRunService.getRecentActivityById(jobId, null, 5))
                .thenReturn(List.of(activity));

        mockMvc.perform(get("/api/test-runs/recent-activity")
                        .param("jobId", jobId.toString())
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].runId").value(activity.getRunId()))
                .andExpect(jsonPath("$[0].jobId").value(jobId.toString()))
                .andExpect(jsonPath("$[0].testName").value("Endpoint_Insight_Health"))
                .andExpect(jsonPath("$[0].group").value("Daily"))
                .andExpect(jsonPath("$[0].durationMs").value(6254))
                .andExpect(jsonPath("$[0].startedBy").value("tester"))
                .andExpect(jsonPath("$[0].status").value("PASS"));
    }

    @Test
    void getRecentActivity_withBatchId_returnsList() throws Exception {
        UUID batchId = UUID.randomUUID();

        RecentActivityDTO activity = RecentActivityDTO.builder()
                .runId(UUID.randomUUID().toString())
                .jobId(UUID.randomUUID().toString())
                .testName("Batch_Test")
                .group("Weekly")
                .dateRun(Instant.parse("2025-07-10T10:15:30Z"))
                .durationMs(5000)
                .startedBy("tester")
                .status("PASS")
                .build();

        when(testRunService.getRecentActivityById(null, batchId, 5))
                .thenReturn(List.of(activity));

        mockMvc.perform(get("/api/test-runs/recent-activity")
                        .param("batchId", batchId.toString())
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].runId").value(activity.getRunId()))
                .andExpect(jsonPath("$[0].testName").value("Batch_Test"))
                .andExpect(jsonPath("$[0].group").value("Weekly"))
                .andExpect(jsonPath("$[0].durationMs").value(5000))
                .andExpect(jsonPath("$[0].status").value("PASS"));
    }

    @Test
    void getRecentActivity_withBothParams_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/test-runs/recent-activity")
                        .param("jobId", UUID.randomUUID().toString())
                        .param("batchId", UUID.randomUUID().toString()))
                .andExpect(status().isBadRequest());
    }

	@Test
	void getTestRunById_returnsRun() throws Exception {
		UUID runId = UUID.randomUUID();
		TestRun run = new TestRun();
		run.setRunId(runId);
		run.setJobId(UUID.randomUUID());
		run.setBatchId(UUID.randomUUID());
		run.setRunBy("tester");
		run.setStatus(TestRunStatus.COMPLETED);

		when(testRunService.getTestRunById(runId)).thenReturn(run);

		mockMvc.perform(get("/api/test-runs/{id}", runId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.runId").value(runId.toString()))
				.andExpect(jsonPath("$.runBy").value("tester"));
	}

	@Test
	void getTestRunById_missingRun_returnsNotFound() throws Exception {
		UUID runId = UUID.randomUUID();
		when(testRunService.getTestRunById(runId)).thenThrow(new TestRunNotFoundException(runId.toString()));

		mockMvc.perform(get("/api/test-runs/{id}", runId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("TEST_RUN_NOT_FOUND"));
	}

	@Test
	void deleteTestRun_returnsOk() throws Exception {
		UUID runId = UUID.randomUUID();

		when(testRunService.deleteTestRunById(runId))
				.thenReturn(ResponseEntity.ok(Map.of("status", "Test run deleted successfully")));

		mockMvc.perform(delete("/api/test-runs/{id}", runId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("Test run deleted successfully"));

		verify(testRunService).deleteTestRunById(runId);
	}

	@Test
	void deleteTestRun_missingRun_returnsNotFound() throws Exception {
		UUID runId = UUID.randomUUID();
		doThrow(new TestRunNotFoundException(runId.toString()))
				.when(testRunService)
				.deleteTestRunById(runId);

		mockMvc.perform(delete("/api/test-runs/{id}", runId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("TEST_RUN_NOT_FOUND"));
	}
}
