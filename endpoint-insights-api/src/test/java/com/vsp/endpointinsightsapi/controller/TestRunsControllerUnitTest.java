package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.model.TestRunCreateRequest;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.service.TestRunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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
		run.setStatus(TestRunStatus.PASS);
		run.setFinishedAt(Instant.now());

		when(testRunService.getRecentTestRuns(5)).thenReturn(List.of(run));

		mockMvc.perform(get("/api/test-runs/recent").param("limit", "5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].runId").value(run.getRunId().toString()))
				.andExpect(jsonPath("$[0].jobId").value(run.getJobId().toString()))
				.andExpect(jsonPath("$[0].runBy").value("tester"))
				.andExpect(jsonPath("$[0].status").value("PASS"));
	}

	@Test
	void createTestRun_returnsCreated() throws Exception {
		TestRun run = new TestRun();
		run.setRunId(UUID.randomUUID());
		run.setJobId(UUID.randomUUID());
        run.setBatchId(UUID.randomUUID());
		run.setRunBy("tester");
		run.setStatus(TestRunStatus.PASS);
		run.setStartedAt(Instant.now());

		TestRunCreateRequest request = new TestRunCreateRequest(
			run.getJobId(),
			run.getRunBy(),
			run.getStatus(),
            run.getBatchId(),
			run.getStartedAt(),
			run.getFinishedAt()
		);

		when(testRunService.createTestRun(any(TestRun.class))).thenReturn(run);

		mockMvc.perform(post("/api/test-runs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.runId").value(run.getRunId().toString()))
				.andExpect(jsonPath("$.runBy").value("tester"))
				.andExpect(jsonPath("$.status").value("PASS"));
	}

	@Test
	void createTestRun_runtimeException_returnsServerError() throws Exception {
		TestRunCreateRequest request = new TestRunCreateRequest(
			UUID.randomUUID(),
			"tester",
			TestRunStatus.PASS,
            UUID.randomUUID(),
			null,
            null
		);

		doThrow(new RuntimeException("test error"))
				.when(testRunService)
				.createTestRun(any(TestRun.class));

		mockMvc.perform(post("/api/test-runs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isInternalServerError());
	}
}
