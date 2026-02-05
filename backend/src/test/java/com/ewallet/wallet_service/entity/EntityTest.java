package com.ewallet.wallet_service.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EntityTest {

    @Test
    void testUserAndWalletEntities() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("password");
        
        Wallet wallet = new Wallet();
        wallet.setId(10L);
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal("500.00"));
        
        assertEquals(1L, user.getId());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("password", user.getPassword());
        
        assertEquals(10L, wallet.getId());
        assertEquals(new BigDecimal("500.00"), wallet.getBalance());
        assertEquals(user, wallet.getUser());
    }

    @Test
    void testAuditLogEntityMinimal() {
        AuditLog log = new AuditLog();
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        
        assertEquals("SUCCESS", log.getStatus());
        assertNotNull(log.getTimestamp());
    }
}