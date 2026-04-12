package com.vsp.endpointinsightsapi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AuditedDTO {
    private UserInfoDTO createdBy;
    private Instant createdDate;
    private UserInfoDTO updatedBy;
    private Instant updatedDate;

    @Data
    public static class UserInfoDTO {
        private String name;
        private String email;
        private String role;
        private String issuer;
        private String subject;
    }
}