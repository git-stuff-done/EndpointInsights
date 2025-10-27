package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.model.JobCreateRequest;
import com.vsp.endpointinsightsapi.model.JobUpdateRequest;
import com.vsp.endpointinsightsapi.validation.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.authentication.enabled=false")
@WebMvcTest(JobsController.class)
@AutoConfigureWebMvc
public class JobControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;


	@Test
	public void createJob() throws Exception {
		mockMvc.perform(post("/api/jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new JobCreateRequest())))
				.andExpect(status().isOk());
	}

	@Test
	public void getJobSuccess() throws Exception {
		mockMvc.perform(get("/api/jobs/{id}", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.jobId").value("1"));
	}

	@Test
	public void getJobInvalidFormat() throws Exception {
		mockMvc.perform(get("/api/jobs/invalidFormat"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.details[0]").value(ErrorMessages.JOB_ID_INVALID_FORMAT));
	}

	@Test
	public void updateJob() throws Exception {
		mockMvc.perform(put("/api/jobs/{id}", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new JobUpdateRequest())))
				.andExpect(status().isOk());
	}

	@Test
	public void deleteJob() throws Exception {
		mockMvc.perform(delete("/api/jobs/{id}", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value("Job 1 deleted"));
	}

	@Test
	public void getJobsSuccess() throws Exception {
		mockMvc.perform(get("/api/jobs")).andExpect(status().isOk());
	}

}
