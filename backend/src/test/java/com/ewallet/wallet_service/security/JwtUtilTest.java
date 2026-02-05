package com.ewallet.wallet_service.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil = new JwtUtil(); 

    @Test
    void testTokenGenerationAndValidation() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email);
        
        assertNotNull(token);
        assertEquals(email, jwtUtil.extractEmail(token));
        assertTrue(jwtUtil.isTokenValid(token));
    }
}