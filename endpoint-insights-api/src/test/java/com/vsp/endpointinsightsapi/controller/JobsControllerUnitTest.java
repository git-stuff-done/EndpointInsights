package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(JobsController.class)
class JobsControllerUnitTest {

    @MockitoBean
    private JobService jobService;

    @Test
    void contextLoads() {
    }
}