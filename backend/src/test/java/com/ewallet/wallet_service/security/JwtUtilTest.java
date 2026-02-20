package com.ewallet.wallet_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String testEmail = "test@koshpay.com";
    private String testRole = "ROLE_USER";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken(testEmail, testRole);
        
        assertNotNull(token);
        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals(testEmail, jwtUtil.extractEmail(token));
        assertEquals(testRole, jwtUtil.extractRole(token));
    }

    @Test
    void testIsTokenValid_InvalidToken() {
        assertFalse(jwtUtil.isTokenValid("invalid.token.here"));
        assertFalse(jwtUtil.isTokenValid(null));
    }
}