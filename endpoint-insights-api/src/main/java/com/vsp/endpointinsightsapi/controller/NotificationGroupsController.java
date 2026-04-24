package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.authentication.RequiredRoles;
import com.vsp.endpointinsightsapi.model.NotificationGroup;
import com.vsp.endpointinsightsapi.model.enums.UserRole;
import com.vsp.endpointinsightsapi.service.NotificationGroupService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notification-groups")
public class NotificationGroupsController {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationGroupsController.class);

    private final NotificationGroupService notificationGroupService;

    public NotificationGroupsController(NotificationGroupService notificationGroupService) {
        this.notificationGroupService = notificationGroupService;
    }

    // GET /api/notification-groups - List all groups
    @GetMapping
    @RequiredRoles(roles = {UserRole.READ})
    public ResponseEntity<List<NotificationGroup>> getAllGroups() {
        List<NotificationGroup> groups = notificationGroupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    // GET /api/notification-groups/{id} - Get specific group with members
    @GetMapping("/{id}")
    @RequiredRoles(roles = {UserRole.READ})
    public ResponseEntity<NotificationGroup> getGroup(@PathVariable UUID id) {
        return notificationGroupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/notification-groups - Create new group
    @PostMapping
    @RequiredRoles(roles = {UserRole.WRITE})
    public ResponseEntity<NotificationGroup> createGroup(@RequestBody CreateGroupRequest request) {
        LOG.info("Creating new notification group: {}", request.getName());
        NotificationGroup group = notificationGroupService.createGroup(
                request.getName(),
                request.getDescription(),
                request.getMembers()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    // PUT /api/notification-groups/{id} - Update existing group
    @PutMapping("/{id}")
    @RequiredRoles(roles = {UserRole.WRITE})
    public ResponseEntity<NotificationGroup> updateGroup(
            @PathVariable @NotNull UUID id,
            @RequestBody UpdateGroupRequest request) {
        LOG.info("Updating notification group: {}", id);
        NotificationGroup group = notificationGroupService.updateGroup(
                id,
                request.getName(),
                request.getDescription()
        );
        return ResponseEntity.ok(group);
    }

    // DELETE /api/notification-groups/{id} - Delete group
    @DeleteMapping("/{id}")
    @RequiredRoles(roles = {UserRole.WRITE})
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID id) {
        LOG.info("Deleting notification group: {}", id);
        notificationGroupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/notification-groups/{id}/members - Add members to group
    @PostMapping("/{id}/members")
    @RequiredRoles(roles = {UserRole.WRITE})
    public ResponseEntity<Void> addMembers(
            @PathVariable @NotNull UUID id,
            @RequestBody AddMembersRequest request) {
        LOG.info("Adding {} members to group {}", request.getEmails().size(), id);
        notificationGroupService.addMembersToGroup(id, request.getEmails());
        return ResponseEntity.ok().build();
    }

    // DELETE /api/notification-groups/{id}/members/{email} - Remove member from group
    @DeleteMapping("/{id}/members/{email}")
    @RequiredRoles(roles = {UserRole.WRITE})
    public ResponseEntity<Void> removeMember(
            @PathVariable @NotNull UUID id,
            @PathVariable String email) {
        LOG.info("Removing member {} from group {}", email, id);
        notificationGroupService.removeMemberFromGroup(id, email);
        return ResponseEntity.noContent().build();
    }

    // DTO Classes
    public static class CreateGroupRequest {
        private String name;
        private String description;
        private List<String> members;

        public CreateGroupRequest() {}

        public CreateGroupRequest(String name, String description, List<String> members) {
            this.name = name;
            this.description = description;
            this.members = members;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getMembers() {
            return members;
        }

        public void setMembers(List<String> members) {
            this.members = members;
        }
    }

    public static class UpdateGroupRequest {
        private String name;
        private String description;

        public UpdateGroupRequest() {}

        public UpdateGroupRequest(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class AddMembersRequest {
        private List<String> emails;

        public AddMembersRequest() {}

        public AddMembersRequest(List<String> emails) {
            this.emails = emails;
        }

        public List<String> getEmails() {
            return emails;
        }

        public void setEmails(List<String> emails) {
            this.emails = emails;
        }
    }
}
