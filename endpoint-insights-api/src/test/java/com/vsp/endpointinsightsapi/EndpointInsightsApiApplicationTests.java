package com.vsp.endpointinsightsapi;

import com.vsp.endpointinsightsapi.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class EndpointInsightsApiApplicationTests {

	@MockitoBean
	private JobRepository jobRepository;

	@Test
	void contextLoads() {
	}
}
