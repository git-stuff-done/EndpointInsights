package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.NotificationGroup;
import com.vsp.endpointinsightsapi.model.entity.NotificationGroupMember;
import com.vsp.endpointinsightsapi.repository.NotificationGroupRepository;
import com.vsp.endpointinsightsapi.repository.NotificationGroupMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationGroupServiceTest {

    private NotificationGroupRepository groupRepository;
    private NotificationGroupMemberRepository memberRepository;
    private NotificationGroupService notificationGroupService;

    @BeforeEach
    void setUp() {
        groupRepository = mock(NotificationGroupRepository.class);
        memberRepository = mock(NotificationGroupMemberRepository.class);
        notificationGroupService = new NotificationGroupService(groupRepository, memberRepository);
    }

    @Test
    void getAllGroups_returnsAllGroups() {
        UUID groupId = UUID.randomUUID();
        NotificationGroup group = new NotificationGroup();
        group.setId(groupId);
        group.setName("Test Group");

        when(groupRepository.findAll()).thenReturn(List.of(group));

        List<NotificationGroup> result = notificationGroupService.getAllGroups();

        assertEquals(1, result.size());
        assertEquals("Test Group", result.get(0).getName());
        verify(groupRepository, times(1)).findAll();
    }

    @Test
    void getGroupById_returnsGroup_whenExists() {
        UUID groupId = UUID.randomUUID();
        NotificationGroup group = new NotificationGroup();
        group.setId(groupId);
        group.setName("Test Group");

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        Optional<NotificationGroup> result = notificationGroupService.getGroupById(groupId);

        assertTrue(result.isPresent());
        assertEquals("Test Group", result.get().getName());
        verify(groupRepository, times(1)).findById(groupId);
    }

    @Test
    void getGroupById_returnsEmpty_whenNotExists() {
        UUID groupId = UUID.randomUUID();

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        Optional<NotificationGroup> result = notificationGroupService.getGroupById(groupId);

        assertFalse(result.isPresent());
        verify(groupRepository, times(1)).findById(groupId);
    }

    @Test
    void createGroup_createsGroupWithMembers() {
        UUID groupId = UUID.randomUUID();
        NotificationGroup group = new NotificationGroup();
        group.setId(groupId);
        group.setName("New Group");
        group.setDescription("Test Description");
        group.setMembers(new ArrayList<>());

        when(groupRepository.save(any(NotificationGroup.class))).thenReturn(group);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        NotificationGroup result = notificationGroupService.createGroup(
            "New Group",
            "Test Description",
            List.of("test@example.com")
        );

        assertEquals("New Group", result.getName());
        assertEquals("Test Description", result.getDescription());
        verify(groupRepository, times(1)).save(any(NotificationGroup.class));
        verify(memberRepository, times(1)).save(any(NotificationGroupMember.class));
    }

    @Test
    void createGroup_createsGroupWithoutMembers() {
        UUID groupId = UUID.randomUUID();
        NotificationGroup group = new NotificationGroup();
        group.setId(groupId);
        group.setName("Empty Group");
        group.setMembers(new ArrayList<>());

        when(groupRepository.save(any(NotificationGroup.class))).thenReturn(group);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        NotificationGroup result = notificationGroupService.createGroup(
            "Empty Group",
            null,
            null
        );

        assertEquals("Empty Group", result.getName());
        verify(groupRepository, times(1)).save(any(NotificationGroup.class));
        verify(memberRepository, never()).save(any());
    }

    @Test
    void updateGroup_updatesGroupSuccessfully() {
        UUID groupId = UUID.randomUUID();
        NotificationGroup group = new NotificationGroup();
        group.setId(groupId);
        group.setName("Old Name");

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.save(any(NotificationGroup.class))).thenReturn(group);

        NotificationGroup result = notificationGroupService.updateGroup(
            groupId,
            "New Name",
            "New Description"
        );

        assertEquals("New Name", group.getName());
        assertEquals("New Description", group.getDescription());
        verify(groupRepository, times(1)).findById(groupId);
        verify(groupRepository, times(1)).save(group);
    }

    @Test
    void updateGroup_throwsException_whenGroupNotFound() {
        UUID groupId = UUID.randomUUID();

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            notificationGroupService.updateGroup(groupId, "New Name", "New Description")
        );
        verify(groupRepository, times(1)).findById(groupId);
        verify(groupRepository, never()).save(any());
    }

    @Test
    void deleteGroup_deletesGroupSuccessfully() {
        UUID groupId = UUID.randomUUID();

        when(groupRepository.existsById(groupId)).thenReturn(true);

        notificationGroupService.deleteGroup(groupId);

        verify(groupRepository, times(1)).existsById(groupId);
        verify(groupRepository, times(1)).deleteById(groupId);
    }

    @Test
    void deleteGroup_logsWarning_whenGroupNotFound() {
        UUID groupId = UUID.randomUUID();

        when(groupRepository.existsById(groupId)).thenReturn(false);

        notificationGroupService.deleteGroup(groupId);

        verify(groupRepository, times(1)).existsById(groupId);
        verify(groupRepository, never()).deleteById(any());
    }

    @Test
    void addMembersToGroup_addsNewMembers() {
        UUID groupId = UUID.randomUUID();
        NotificationGroup group = new NotificationGroup();
        group.setId(groupId);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(memberRepository.findByGroupIdAndEmail(groupId, "test@example.com")).thenReturn(Optional.empty());

        notificationGroupService.addMembersToGroup(groupId, List.of("test@example.com"));

        verify(groupRepository, times(1)).findById(groupId);
        verify(memberRepository, times(1)).save(any(NotificationGroupMember.class));
    }

    @Test
    void addMembersToGroup_skipsDuplicates() {
        UUID groupId = UUID.randomUUID();
        NotificationGroup group = new NotificationGroup();
        group.setId(groupId);

        NotificationGroupMember existingMember = new NotificationGroupMember();
        existingMember.setEmail("test@example.com");

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(memberRepository.findByGroupIdAndEmail(groupId, "test@example.com"))
            .thenReturn(Optional.of(existingMember));

        notificationGroupService.addMembersToGroup(groupId, List.of("test@example.com"));

        verify(memberRepository, never()).save(any(NotificationGroupMember.class));
    }

    @Test
    void addMembersToGroup_throwsException_whenGroupNotFound() {
        UUID groupId = UUID.randomUUID();

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            notificationGroupService.addMembersToGroup(groupId, List.of("test@example.com"))
        );
        verify(memberRepository, never()).save(any());
    }

    @Test
    void removeMemberFromGroup_removesMemberSuccessfully() {
        UUID groupId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        NotificationGroupMember member = new NotificationGroupMember();
        member.setId(memberId);
        member.setEmail("test@example.com");

        when(memberRepository.findByGroupIdAndEmail(groupId, "test@example.com"))
            .thenReturn(Optional.of(member));

        notificationGroupService.removeMemberFromGroup(groupId, "test@example.com");

        verify(memberRepository, times(1)).deleteById(memberId);
    }

    @Test
    void removeMemberFromGroup_logsWarning_whenMemberNotFound() {
        UUID groupId = UUID.randomUUID();

        when(memberRepository.findByGroupIdAndEmail(groupId, "test@example.com"))
            .thenReturn(Optional.empty());

        notificationGroupService.removeMemberFromGroup(groupId, "test@example.com");

        verify(memberRepository, never()).deleteById(any());
    }

    @Test
    void getGroupMemberEmails_returnsAllEmails() {
        UUID groupId = UUID.randomUUID();

        NotificationGroupMember member1 = new NotificationGroupMember();
        member1.setEmail("test1@example.com");

        NotificationGroupMember member2 = new NotificationGroupMember();
        member2.setEmail("test2@example.com");

        when(memberRepository.findAllByGroupId(groupId)).thenReturn(List.of(member1, member2));

        List<String> result = notificationGroupService.getGroupMemberEmails(groupId);

        assertEquals(2, result.size());
        assertTrue(result.contains("test1@example.com"));
        assertTrue(result.contains("test2@example.com"));
        verify(memberRepository, times(1)).findAllByGroupId(groupId);
    }

    @Test
    void resolveGroupsToEmails_resolvesMultipleGroups() {
        UUID groupId1 = UUID.randomUUID();
        UUID groupId2 = UUID.randomUUID();

        NotificationGroupMember member1 = new NotificationGroupMember();
        member1.setEmail("test1@example.com");

        NotificationGroupMember member2 = new NotificationGroupMember();
        member2.setEmail("test2@example.com");

        when(memberRepository.findAllByGroupId(groupId1)).thenReturn(List.of(member1));
        when(memberRepository.findAllByGroupId(groupId2)).thenReturn(List.of(member2));

        List<String> result = notificationGroupService.resolveGroupsToEmails(List.of(groupId1, groupId2));

        assertEquals(2, result.size());
        assertTrue(result.contains("test1@example.com"));
        assertTrue(result.contains("test2@example.com"));
    }

    @Test
    void resolveGroupsToEmails_returnsEmpty_whenNoGroups() {
        List<String> result = notificationGroupService.resolveGroupsToEmails(null);

        assertEquals(0, result.size());
        verify(memberRepository, never()).findAllByGroupId(any());
    }

    @Test
    void resolveGroupsToEmails_deduplicatesEmails() {
        UUID groupId1 = UUID.randomUUID();
        UUID groupId2 = UUID.randomUUID();

        NotificationGroupMember member1 = new NotificationGroupMember();
        member1.setEmail("shared@example.com");

        NotificationGroupMember member2 = new NotificationGroupMember();
        member2.setEmail("shared@example.com");

        when(memberRepository.findAllByGroupId(groupId1)).thenReturn(List.of(member1));
        when(memberRepository.findAllByGroupId(groupId2)).thenReturn(List.of(member2));

        List<String> result = notificationGroupService.resolveGroupsToEmails(List.of(groupId1, groupId2));

        assertEquals(1, result.size());
        assertTrue(result.contains("shared@example.com"));
    }
}
