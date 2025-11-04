package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.service.JobService;

import com.vsp.endpointinsightsapi.model.JobCreateRequest;
import com.vsp.endpointinsightsapi.model.JobUpdateRequest;
import com.vsp.endpointinsightsapi.validation.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.doThrow;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.authentication.enabled=false")
@WebMvcTest(JobsController.class)
@AutoConfigureWebMvc
public class JobControllerUnitTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private JobService jobService;


	@Test
	public void createJob() throws Exception {
		mockMvc.perform(post("/api/jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new JobCreateRequest())))
				.andExpect(status().isOk());
	}

	@Test
	public void getJobSuccess() throws Exception {
		UUID jobId = UUID.randomUUID();
		mockMvc.perform(get("/api/jobs/{id}", jobId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.jobId").value(jobId.toString()));
	}

	@Test
	public void updateJob() throws Exception {
		UUID jobId = UUID.randomUUID();
		mockMvc.perform(put("/api/jobs/{id}", jobId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new JobUpdateRequest())))
				.andExpect(status().isOk());
	}

	@Test
	public void deleteJob() throws Exception {
		UUID jobId = UUID.randomUUID();

		mockMvc.perform(delete("/api/jobs/{id}", jobId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value("Job %s deleted".formatted(jobId.toString())));
	}

	@Test
	public void getJobsSuccess() throws Exception {
		mockMvc.perform(get("/api/jobs")).andExpect(status().isOk());
	}

	@Test
	public void deleteJob_runtimeException_returnsNotFound() throws Exception {
		doThrow(new RuntimeException("test error"))
				.when(jobService)
				.deleteJobById("123");

		mockMvc.perform(delete("/api/jobs/{id}", "123"))
				.andExpect(status().isNotFound());
	}
}
