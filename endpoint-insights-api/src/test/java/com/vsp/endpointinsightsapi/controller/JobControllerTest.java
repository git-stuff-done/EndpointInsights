package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.service.JobService;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.JobCreateRequest;
import com.vsp.endpointinsightsapi.model.JobOLD;
import com.vsp.endpointinsightsapi.model.JobUpdateRequest;
import com.vsp.endpointinsightsapi.validation.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.doThrow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobsController.class)
@AutoConfigureWebMvc
public class JobControllerTest {

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
						.content(objectMapper.writeValueAsString(new JobCreateRequest("test_job"))))
				.andExpect(status().isCreated());
	}

	@Test
	public void getJobSuccess() throws Exception {
		Job job = new Job();
		job.setJobId(UUID.fromString("1"));
		when(jobService.getJobById(UUID.fromString("1"))).thenReturn(Optional.of(job));
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
		Job job = new Job();
		job.setJobId(UUID.fromString("01"));
		jobService.createJob(job);
		Optional<List<Job>> jobs = Optional.of(List.of(job));
		when(jobService.getAllJobs()).thenReturn(jobs);
		mockMvc.perform(get("/api/jobs")).andExpect(status().isOk());
	}


	@Test
	public void deleteJob_runtimeException_returnsNotFound() throws Exception {
		doThrow(new RuntimeException("test error"))
				.when(jobService)
				.deleteJobById(UUID.fromString("123"));

		mockMvc.perform(delete("/api/jobs/{id}", "123"))
				.andExpect(status().isNotFound());
	}
}
