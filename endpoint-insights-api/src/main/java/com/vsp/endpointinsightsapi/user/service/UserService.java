package com.vsp.endpointinsightsapi.user.service;

import com.vsp.endpointinsightsapi.user.model.User;
import com.vsp.endpointinsightsapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllusers(){
        return userRepository.findAll();
    }

    public List<User> getAllUsersBasedOnQuery(String query){
        return userRepository.findAllMatching(query);
    }

    public List<User> findAllByIds(List<String> ids){
        List<UUID> idsAsUuid = ids.stream().map(UUID::fromString).toList();
        return userRepository.findAllById(idsAsUuid);
    }
}
