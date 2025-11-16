package com.vsp.endpointinsightsapi.config;


import com.vsp.endpointinsightsapi.model.UserContext;
import com.vsp.endpointinsightsapi.util.CurrentUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class EntityAuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {

        return () -> {
            if(!CurrentUser.getUsername().equals("system")) {
                return Optional.of(CurrentUser.getUsername());
            }
            return Optional.of("system");
        };
    }

}
