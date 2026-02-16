package com.vsp.endpointinsightsrunnerapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.vsp.endpointinsightsapi.model"})
@EnableJpaRepositories(basePackages = {"com.vsp.endpointinsightsapi.repository"})
public class EndpointInsightsRunnerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EndpointInsightsRunnerApiApplication.class, args);
	}

}
