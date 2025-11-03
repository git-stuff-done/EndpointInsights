package com.vsp.endpointinsightsapi;

import com.vsp.endpointinsightsapi.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
class EndpointInsightsApiApplicationTests {

	@MockitoBean
	private JobRepository jobRepository;
	
	@Test
	void contextLoads() {
	}

}
