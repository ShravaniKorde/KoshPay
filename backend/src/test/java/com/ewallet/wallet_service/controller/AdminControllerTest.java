package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.service.AdminAnalyticsService;
import com.ewallet.wallet_service.repository.TransactionRepository;
import com.ewallet.wallet_service.repository.AuditLogRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import com.ewallet.wallet_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AdminAnalyticsService analyticsService;
    @MockBean private TransactionRepository transactionRepository;
    @MockBean private AuditLogRepository auditLogRepository;
    @MockBean private VirtualPaymentAddressRepository vpaRepository;

    @Test
    void getSummary_Success() throws Exception {
        mockMvc.perform(get("/api/admin/summary")).andExpect(status().isOk());
    }

    @Test
    void testGetAllTransactions_FullCoverage() throws Exception {
        com.ewallet.wallet_service.entity.User user = new com.ewallet.wallet_service.entity.User();
        user.setId(1L);
        
        com.ewallet.wallet_service.entity.Wallet wallet = new com.ewallet.wallet_service.entity.Wallet();
        wallet.setUser(user);

        com.ewallet.wallet_service.entity.Transaction tx = new com.ewallet.wallet_service.entity.Transaction();
        tx.setFromWallet(wallet);
        tx.setToWallet(wallet);
        tx.setAmount(java.math.BigDecimal.TEN);
        tx.setStatus(com.ewallet.wallet_service.entity.TransactionStatus.SUCCESS);

        when(transactionRepository.findAll()).thenReturn(List.of(tx));
        when(vpaRepository.findByUserId(any())).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/admin/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromUpiId").value("N/A"));
    }
}