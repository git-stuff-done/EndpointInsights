package com.vsp.endpointinsightsapi.model;

import com.vsp.endpointinsightsapi.model.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserContext {
    private final String userId;
    private final String username;
    private final String email;
    private final UserRole role;

    public String getLogIdentifier() {
        return String.format("%s (%s)", username, userId);
    }

    public boolean hasWriteAccess() {
        return role == UserRole.WRITE;
    }

    public boolean hasReadAccess() {
        return role == UserRole.READ || role == UserRole.WRITE;
    }
}