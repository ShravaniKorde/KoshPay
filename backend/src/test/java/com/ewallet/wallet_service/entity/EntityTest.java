package com.ewallet.wallet_service;

import com.ewallet.wallet_service.entity.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testEntities() {
        Admin admin = new Admin();
        admin.setEmail("admin@koshpay.com");
        admin.setPassword("hash");
        assertNotNull(admin.getCreatedAt());
        assertEquals("admin@koshpay.com", admin.getEmail());

        AuditLog log = new AuditLog();
        log.setActionType("TRANSFER");
        log.setOldBalance(BigDecimal.ONE);
        log.setNewBalance(BigDecimal.ZERO);
        assertNotNull(log.getTimestamp());

        User user = new User();
        user.setName("John");
        user.setTransactionPin("1234");
        user.setOtpExpiry(LocalDateTime.now());
        assertEquals("John", user.getName());

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.valueOf(1000));
        assertEquals(BigDecimal.valueOf(1000), wallet.getBalance());

        VirtualPaymentAddress vpa = new VirtualPaymentAddress();
        vpa.setUpiId("john@koshpay");
        vpa.setUser(user);
        vpa.setActive(true);
        assertTrue(vpa.isActive());

        Contact contact = new Contact();
        contact.setOwner(user);
        contact.setDisplayName("Friend");
        contact.setUpiId("friend@upi");
        assertEquals("friend@upi", contact.getUpiId());

        Transaction tx = new Transaction();
        tx.setFromWallet(wallet);
        tx.setAmount(BigDecimal.TEN);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setTimestamp(Instant.now());
        assertEquals(TransactionStatus.SUCCESS, tx.getStatus());

        ScheduledPayment sp = new ScheduledPayment();
        sp.setAmount(BigDecimal.valueOf(50));
        sp.setSender(user);
        sp.setReceiver(vpa);
        sp.setExecuted(false);
        assertFalse(sp.isExecuted());
        
        assertNotNull(TransactionStatus.valueOf("PENDING"));
    }
}