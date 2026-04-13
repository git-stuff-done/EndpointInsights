package com.vsp.endpointinsightsapi.mapper;

import com.vsp.endpointinsightsapi.dto.NotificationGroupDTO;
import com.vsp.endpointinsightsapi.model.NotificationGroup;
import com.vsp.endpointinsightsapi.model.entity.NotificationGroupMember;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationGroupMapper {

    public NotificationGroupDTO toDto(NotificationGroup group) {
        if (group == null) {
            return null;
        }

        NotificationGroupDTO dto = new NotificationGroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setMembers(mapMembers(group.getMembers()));

        return dto;
    }

    private List<String> mapMembers(List<NotificationGroupMember> members) {
        if (members == null) {
            return List.of();
        }
        return members.stream()
                .map(NotificationGroupMember::getEmail)
                .collect(Collectors.toList());
    }
}
