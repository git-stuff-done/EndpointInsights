package com.vsp.endpointinsightsapi.service;


import com.vsp.endpointinsightsapi.user.model.User;
import com.vsp.endpointinsightsapi.user.repository.UserRepository;
import com.vsp.endpointinsightsapi.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsersServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setName("Alice");

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setName("Bob");

        List<User> expectedUsers = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<User> result = userService.getAllusers();

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Bob", result.get(1).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_WhenEmpty_ShouldReturnEmptyList() {

        when(userRepository.findAll()).thenReturn(List.of());
        List<User> result = userService.getAllusers();

        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsersBasedOnQuery_ShouldReturnMatchingUsers() {
        String query = "Alice";
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setName("Alice");

        List<User> expectedUsers = List.of(user1);
        when(userRepository.findAllMatching(query)).thenReturn(expectedUsers);

        List<User> result = userService.getAllUsersBasedOnQuery(query);

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
        verify(userRepository, times(1)).findAllMatching(query);
    }

    @Test
    void getAllUsersBasedOnQuery_WhenNoMatches_ShouldReturnEmptyList() {
        String query = "NonExistent";
        when(userRepository.findAllMatching(query)).thenReturn(List.of());
        List<User> result = userService.getAllUsersBasedOnQuery(query);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAllMatching(query);
    }

    @Test
    void findAllByIds_ShouldReturnUsersWithMatchingIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<String> idsAsStrings = List.of(id1.toString(), id2.toString());

        User user1 = new User();
        user1.setId(id1);
        user1.setName("Alice");

        User user2 = new User();
        user2.setId(id2);
        user2.setName("Bob");

        List<User> expectedUsers = List.of(user1, user2);
        when(userRepository.findAllById(List.of(id1, id2))).thenReturn(expectedUsers);

        List<User> result = userService.findAllByIds(idsAsStrings);

        assertEquals(2, result.size());
        assertEquals(id1, result.get(0).getId());
        assertEquals(id2, result.get(1).getId());
        verify(userRepository, times(1)).findAllById(anyList());
    }

    @Test
    void findAllByIds_WhenEmptyList_ShouldReturnEmptyList() {
        List<String> emptyIds = List.of();
        when(userRepository.findAllById(anyList())).thenReturn(List.of());

        List<User> result = userService.findAllByIds(emptyIds);

        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAllById(anyList());
    }

    @Test
    void findAllByIds_WithInvalidUUID_ShouldThrowException() {
        List<String> invalidIds = List.of("invalid-uuid");

        assertThrows(IllegalArgumentException.class, () -> {
            userService.findAllByIds(invalidIds);
        });
    }

    @Test
    void findAllByIds_WithMixedValidAndInvalidUUIDs_ShouldThrowException() {
        UUID validId = UUID.randomUUID();
        List<String> mixedIds = List.of(validId.toString(), "invalid-uuid");

        assertThrows(IllegalArgumentException.class, () -> {
            userService.findAllByIds(mixedIds);
        });
    }

    @Test
    void findAllByIds_WithNullInList_ShouldThrowException() {
        List<String> idsWithNull = new ArrayList<>();
        idsWithNull.add(null);

        assertThrows(NullPointerException.class, () -> {
            userService.findAllByIds(idsWithNull);
        });
    }


}
