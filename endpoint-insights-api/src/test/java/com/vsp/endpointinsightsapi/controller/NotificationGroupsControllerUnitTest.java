package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.model.NotificationGroup;
import com.vsp.endpointinsightsapi.service.NotificationGroupService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = "app.authentication.enabled=false")
@WebMvcTest(controllers = NotificationGroupsController.class)
class NotificationGroupsControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationGroupService notificationGroupService;

    @Test
    void getAllGroups_returnsEmptyList_whenNoGroups() throws Exception {
        when(notificationGroupService.getAllGroups()).thenReturn(List.of());

        mockMvc.perform(get("/api/notification-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(notificationGroupService, times(1)).getAllGroups();
    }

    @Test
    void getAllGroups_returnsAllGroups() throws Exception {
        UUID groupId = UUID.randomUUID();
        NotificationGroup group = new NotificationGroup();
        group.setId(groupId);
        group.setName("Test Group");
        group.setDescription("Test Description");

        when(notificationGroupService.getAllGroups()).thenReturn(List.of(group));

        mockMvc.perform(get("/api/notification-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Group"))
                .andExpect(jsonPath("$[0].description").value("Test Description"));

        verify(notificationGroupService, times(1)).getAllGroups();
    }

    @Test
    void getGroupById_returnsGroup_whenExists() throws Exception {
        UUID groupId = UUID.randomUUID();
        NotificationGroup group = new NotificationGroup();
        group.setId(groupId);
        group.setName("Test Group");

        when(notificationGroupService.getGroupById(groupId)).thenReturn(Optional.of(group));

        mockMvc.perform(get("/api/notification-groups/" + groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(groupId.toString()))
                .andExpect(jsonPath("$.name").value("Test Group"));

        verify(notificationGroupService, times(1)).getGroupById(groupId);
    }

    @Test
    void getGroupById_returns404_whenNotFound() throws Exception {
        UUID groupId = UUID.randomUUID();

        when(notificationGroupService.getGroupById(groupId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notification-groups/" + groupId))
                .andExpect(status().isNotFound());

        verify(notificationGroupService, times(1)).getGroupById(groupId);
    }

    @Test
    void createGroup_createsGroupSuccessfully() throws Exception {
        UUID groupId = UUID.randomUUID();
        NotificationGroup group = new NotificationGroup();
        group.setId(groupId);
        group.setName("New Group");
        group.setDescription("Test Description");

        String requestBody = objectMapper.writeValueAsString(
            new Object() {
                public final String name = "New Group";
                public final String description = "Test Description";
                public final List<String> members = List.of("test@example.com");
            }
        );

        when(notificationGroupService.createGroup(
            eq("New Group"),
            eq("Test Description"),
            any()
        )).thenReturn(group);

        mockMvc.perform(post("/api/notification-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Group"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(notificationGroupService, times(1)).createGroup(
            eq("New Group"),
            eq("Test Description"),
            any()
        );
    }

    @Test
    void updateGroup_updatesGroupSuccessfully() throws Exception {
        UUID groupId = UUID.randomUUID();
        NotificationGroup group = new NotificationGroup();
        group.setId(groupId);
        group.setName("Updated Group");
        group.setDescription("Updated Description");

        String requestBody = objectMapper.writeValueAsString(
            new Object() {
                public final String name = "Updated Group";
                public final String description = "Updated Description";
            }
        );

        when(notificationGroupService.updateGroup(
            eq(groupId),
            eq("Updated Group"),
            eq("Updated Description")
        )).thenReturn(group);

        mockMvc.perform(put("/api/notification-groups/" + groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Group"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        verify(notificationGroupService, times(1)).updateGroup(
            eq(groupId),
            eq("Updated Group"),
            eq("Updated Description")
        );
    }

    @Test
    void deleteGroup_deletesGroupSuccessfully() throws Exception {
        UUID groupId = UUID.randomUUID();

        doNothing().when(notificationGroupService).deleteGroup(groupId);

        mockMvc.perform(delete("/api/notification-groups/" + groupId))
                .andExpect(status().isNoContent());

        verify(notificationGroupService, times(1)).deleteGroup(groupId);
    }

    @Test
    void addMembers_addsGroupMembersSuccessfully() throws Exception {
        UUID groupId = UUID.randomUUID();

        String requestBody = objectMapper.writeValueAsString(
            new Object() {
                public final List<String> emails = List.of("test1@example.com", "test2@example.com");
            }
        );

        doNothing().when(notificationGroupService).addMembersToGroup(eq(groupId), any());

        mockMvc.perform(post("/api/notification-groups/" + groupId + "/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        verify(notificationGroupService, times(1)).addMembersToGroup(eq(groupId), any());
    }

    @Test
    void removeMember_removesGroupMemberSuccessfully() throws Exception {
        UUID groupId = UUID.randomUUID();
        String email = "test@example.com";

        doNothing().when(notificationGroupService).removeMemberFromGroup(groupId, email);

        mockMvc.perform(delete("/api/notification-groups/" + groupId + "/members/" + email))
                .andExpect(status().isNoContent());

        verify(notificationGroupService, times(1)).removeMemberFromGroup(groupId, email);
    }
}
