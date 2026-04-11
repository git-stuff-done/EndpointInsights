package com.vsp.endpointinsightsapi.config;


import com.vsp.endpointinsightsapi.model.UserIdentity;
import com.vsp.endpointinsightsapi.util.CurrentUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Configuration for JPA auditing using OIDC user identity.
 *
 * <p>Automatically populates createdBy and updatedBy fields with the current
 * user's OIDC identity (issuer/subject) from the JWT token.
 */
@Configuration
@EnableJpaAuditing
public class EntityAuditConfig {

    @Bean
    public AuditorAware<UserIdentity> auditorProvider() {

        return () -> {
            Optional<UserIdentity> userIdentity = CurrentUser.getUserIdentity();
            if (userIdentity.isPresent()) {
                return userIdentity;
            }
            return Optional.of(new UserIdentity("system", "system"));
        };
    }

}
