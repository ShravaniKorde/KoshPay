package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.request.LoginRequest;
import com.ewallet.wallet_service.dto.response.AuthResponse;
import com.ewallet.wallet_service.entity.Admin;
import com.ewallet.wallet_service.repository.AdminRepository;
import com.ewallet.wallet_service.security.JwtUtil;
import com.ewallet.wallet_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock private UserService userService;
    @Mock private AdminRepository adminRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testAdminLoginSuccess() throws Exception {
        Admin admin = new Admin();
        admin.setEmail("admin@test.com");
        admin.setPassword("hashed_pass");

        when(adminRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generateToken(any(), any())).thenReturn("admin_token");

        String json = "{\"email\":\"admin@test.com\", \"password\":\"pass\", \"adminLogin\":true}";
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin_token"));
    }

    @Test
    void testUserLogin() throws Exception {
        when(userService.login(any())).thenReturn(new AuthResponse("user_token"));

        String json = "{\"email\":\"user@test.com\", \"password\":\"pass\", \"adminLogin\":false}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testAdminLogin_InvalidCredentials() {
        when(adminRepository.findByEmail(any())).thenReturn(java.util.Optional.empty());

        String json = "{\"email\":\"wrong@admin.com\", \"password\":\"pass\", \"adminLogin\":true}";

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));
        });

        assertTrue(exception.getCause() instanceof com.ewallet.wallet_service.exception.InvalidRequestException);
    }
}