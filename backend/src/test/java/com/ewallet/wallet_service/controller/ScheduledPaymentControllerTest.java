package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.service.ScheduledPaymentService;
import com.ewallet.wallet_service.entity.TransactionStatus; // Import your Enum
import com.ewallet.wallet_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq; 
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath; 

@WebMvcTest(controllers = ScheduledPaymentController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ScheduledPaymentControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private ScheduledPaymentService scheduledPaymentService;

    @Test
    void testCreateSchedule() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("test@test.com", "password");
        
        com.ewallet.wallet_service.entity.ScheduledPayment payment = new com.ewallet.wallet_service.entity.ScheduledPayment();
        payment.setId(100L);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setExecuted(false);
        
        payment.setStatus(TransactionStatus.PENDING); 
        
        payment.setScheduledAt(Instant.now());

        com.ewallet.wallet_service.entity.User sender = new com.ewallet.wallet_service.entity.User();
        sender.setEmail("test@test.com");
        payment.setSender(sender);

        com.ewallet.wallet_service.entity.VirtualPaymentAddress receiver = new com.ewallet.wallet_service.entity.VirtualPaymentAddress();
        receiver.setUpiId("receiver@upi");
        payment.setReceiver(receiver);

        when(scheduledPaymentService.createSchedule(
                eq("test@test.com"), 
                anyString(), 
                any(BigDecimal.class), 
                any(Instant.class)) 
        ).thenReturn(payment);

        String json = "{\"receiverUpiId\":\"receiver@upi\", \"amount\":100, \"scheduledAt\":\"2026-12-31T10:00:00Z\"}";

        mockMvc.perform(post("/api/scheduled-payments")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.amount").value(100));
    }
}