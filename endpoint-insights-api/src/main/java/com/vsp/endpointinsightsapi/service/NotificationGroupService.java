package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.NotificationGroup;
import com.vsp.endpointinsightsapi.model.entity.NotificationGroupMember;
import com.vsp.endpointinsightsapi.repository.NotificationGroupRepository;
import com.vsp.endpointinsightsapi.repository.NotificationGroupMemberRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationGroupService.class);

    private final NotificationGroupRepository groupRepository;
    private final NotificationGroupMemberRepository memberRepository;

    public NotificationGroupService(NotificationGroupRepository groupRepository,
                                   NotificationGroupMemberRepository memberRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    // Get all notification groups
    public List<NotificationGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    // Get a specific group by ID
    public Optional<NotificationGroup> getGroupById(UUID id) {
        return groupRepository.findById(id);
    }

    // Create a new notification group
    @Transactional
    public NotificationGroup createGroup(String name, String description, List<String> memberEmails) {
        NotificationGroup group = new NotificationGroup();
        group.setName(name);
        group.setDescription(description);
        group.setMembers(new ArrayList<>());

        NotificationGroup savedGroup = groupRepository.save(group);

        if (memberEmails != null && !memberEmails.isEmpty()) {
            addMembersToGroup(savedGroup.getId(), memberEmails);
        }

        return groupRepository.findById(savedGroup.getId()).orElseThrow();
    }

    // Update an existing group
    @Transactional
    public NotificationGroup updateGroup(UUID id, String name, String description) {
        NotificationGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification group not found: " + id));

        group.setName(name);
        group.setDescription(description);

        return groupRepository.save(group);
    }

    // Delete a group (cascades to delete members)
    @Transactional
    public void deleteGroup(UUID id) {
        if (!groupRepository.existsById(id)) {
            LOG.warn("Attempted to delete non-existent group: {}", id);
            return;
        }
        groupRepository.deleteById(id);
        LOG.info("Deleted notification group: {}", id);
    }

    // Add members to a group
    @Transactional
    public void addMembersToGroup(UUID groupId, List<String> emails) {
        NotificationGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Notification group not found: " + groupId));

        for (String email : emails) {
            // Check if member already exists
            if (memberRepository.findByGroupIdAndEmail(groupId, email).isEmpty()) {
                NotificationGroupMember member = new NotificationGroupMember();
                member.setGroupId(groupId);
                member.setEmail(email);
                memberRepository.save(member);
            }
        }
    }

    // Remove a member from a group
    @Transactional
    public void removeMemberFromGroup(UUID groupId, String email) {
        memberRepository.findByGroupIdAndEmail(groupId, email)
                .ifPresentOrElse(
                    member -> memberRepository.deleteById(member.getId()),
                    () -> LOG.warn("Member {} not found in group {}", email, groupId)
                );
    }

    // Get all email addresses for a group
    public List<String> getGroupMemberEmails(UUID groupId) {
        List<NotificationGroupMember> members = memberRepository.findAllByGroupId(groupId);
        return members.stream()
                .map(NotificationGroupMember::getEmail)
                .collect(Collectors.toList());
    }

    // Get all member emails for a list of group IDs (used for batch notifications)
    public List<String> resolveGroupsToEmails(List<UUID> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new ArrayList<>();
        }

        return groupIds.stream()
                .flatMap(groupId -> getGroupMemberEmails(groupId).stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
