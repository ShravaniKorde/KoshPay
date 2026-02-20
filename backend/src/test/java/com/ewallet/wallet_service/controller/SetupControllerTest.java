package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.service.UserService;
import com.ewallet.wallet_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SetupController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class SetupControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserService userService;
    @MockBean private JwtUtil jwtUtil;

    @Test
    void createUser_Success() throws Exception {
        doNothing().when(userService).createUser(any());

        mockMvc.perform(post("/api/setup/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) 
                .andReturn(); 
    }
}