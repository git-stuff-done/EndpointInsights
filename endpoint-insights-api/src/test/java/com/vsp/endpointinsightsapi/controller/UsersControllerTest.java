package com.vsp.endpointinsightsapi.controller;


import com.vsp.endpointinsightsapi.service.BatchService;
import com.vsp.endpointinsightsapi.user.controller.UserController;
import com.vsp.endpointinsightsapi.user.model.User;
import com.vsp.endpointinsightsapi.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = "app.authentication.enabled=false")
@WebMvcTest(UserController.class)
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;


    @Test
    void get_all_users() throws Exception {
        when(userService.getAllusers()).thenReturn(List.of(new User()));
        mockMvc.perform(get("/api/users")).andExpect(status().isOk());
    }


    @Test
    void get_all_users_based_on_query() throws Exception {
        String query = "Str";
        when(userService.getAllUsersBasedOnQuery(query)).thenReturn(List.of(new User()));
        mockMvc.perform(get("/api/users/matching-query")
                .param("query", query)).andExpect(status().isOk());
    }


    @Test
    void find_all_by_user_id() throws Exception {
        List<String> ids = List.of("abc", "degf");
        when(userService.findAllByIds(ids)).thenReturn(List.of(new User()));
        mockMvc.perform(get("/api/users/find-by-ids")
                .param("idList", String.valueOf(ids))).andExpect(status().isOk());
    }

}
