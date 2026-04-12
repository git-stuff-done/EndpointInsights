package com.vsp.endpointinsightsapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "test-runs")
@Getter
@Setter
public class TestRunsConfig {

    @Value("${test-runs.purge-frequency.hours}")
    private int purgeFrequencyHours;
    @Value("${test-runs.purge-frequency.minutes}")
    private int purgeFrequencyMinutes;

    @Value("${test-runs.max-age.months}")
    private int maxAgeMonths;
    @Value("${test-runs.max-age.days}")
    private int maxAgeDays;
    @Value("${test-runs.max-age.hours}")
    private int maxAgeHours;
    @Value("${test-runs.max-age.minutes}")
    private int maxAgeMinutes;
    @Value("${test-runs.max-age.seconds}")
    private int maxAgeSeconds;

}
