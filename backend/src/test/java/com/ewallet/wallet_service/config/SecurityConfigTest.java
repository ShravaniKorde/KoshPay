package com.ewallet.wallet_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.web.SecurityFilterChain;
import com.ewallet.wallet_service.security.JwtUtil;
import com.ewallet.wallet_service.security.JwtFilter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SecurityConfigTest {

    @Autowired(required = false)
    private SecurityFilterChain securityFilterChain;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private JwtFilter jwtFilter;

    @Test
    void securityContextLoads() {
        assertNotNull(securityFilterChain, "Security Filter Chain should be initialized");
    }
}