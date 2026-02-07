package com.vsp.endpointinsightsapi.user.controller;


import com.vsp.endpointinsightsapi.user.model.User;
import com.vsp.endpointinsightsapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers(){
        return userService.getAllusers();
    }

    @GetMapping("/matching-query")
    public List<User> getAllUsersBasedOnQueryString(@RequestParam("query") String query){
        return userService.getAllUsersBasedOnQuery(query);
    }

    @GetMapping("/find-by-ids")
    public List<User> findByUserId(@RequestParam List<String> idList){
        return userService.findAllByIds(idList);
    }
}
