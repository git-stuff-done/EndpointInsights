package com.vsp.endpointinsightsapi.controller;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@Tag("unit")
@WebMvcTest(BatchesController.class)
class BatchesControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoad() {

    }
}