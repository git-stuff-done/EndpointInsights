package com.vsp.endpointinsightsapi.mapper;

import com.vsp.endpointinsightsapi.dto.AuditedDTO;
import com.vsp.endpointinsightsapi.model.AuditingEntity;
import com.vsp.endpointinsightsapi.model.User;

public abstract class AuditedMapper {

    /**
     * Maps audit fields from entity to DTO
     */
    protected void mapAuditFields(AuditingEntity entity, AuditedDTO dto) {
        if (entity.getCreator() != null) {
            dto.setCreatedBy(mapUserInfo(entity.getCreator()));
        }
        dto.setCreatedDate(entity.getCreatedDate());

        if (entity.getUpdater() != null) {
            dto.setUpdatedBy(mapUserInfo(entity.getUpdater()));
        }
        dto.setUpdatedDate(entity.getUpdatedDate());
    }

    /**
     * Maps User entity to UserInfoDTO
     */
    protected AuditedDTO.UserInfoDTO mapUserInfo(User user) {
        AuditedDTO.UserInfoDTO userInfo = new AuditedDTO.UserInfoDTO();
        userInfo.setName(user.getName());
        userInfo.setRole(String.valueOf(user.getRole()));
        userInfo.setEmail(user.getEmail());
        userInfo.setIssuer(user.getIssuer());
        userInfo.setSubject(user.getSubject());
        return userInfo;
    }
}