package com.vsp.endpointinsightsapi.controller;

import org.junit.jupiter.api.Tag;
import com.vsp.endpointinsightsapi.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(JobsController.class)
class JobsControllerUnitTest {

    @MockitoBean
    private JobService jobService;

    @Test
    void contextLoads() {
    }
}