package com.vsp.endpointinsightsapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ConfigurationProperties(prefix = "scheduling")
public class SchedulingConfig {

	@Getter
	@Setter
	private int poolSize = 10;

	@Getter
	@Setter
	private String threadNamePrefix = "TestBatchScheduler-";


	@Bean(name = "vspTaskScheduler")
	public ThreadPoolTaskScheduler taskScheduler() {
		var scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(poolSize);
		scheduler.setThreadNamePrefix(threadNamePrefix);
		return scheduler;
	}
}
