package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.response.WalletResponse;
import com.ewallet.wallet_service.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WalletControllerTest {

    private MockMvc mockMvc;
    @Mock private WalletService walletService;
    @InjectMocks private WalletController walletController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(walletController).build();
    }

    @Test
    void testGetMyBalance() throws Exception {
        when(walletService.getMyBalance()).thenReturn(new WalletResponse(1L, BigDecimal.valueOf(500.0)));
        mockMvc.perform(get("/api/wallet/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.0));
    }

    @Test
    void testGetMyTransactions_WithData() throws Exception {
        com.ewallet.wallet_service.dto.response.TransactionResponse tx = 
            new com.ewallet.wallet_service.dto.response.TransactionResponse();
        tx.setAmount(BigDecimal.valueOf(100.0));
        tx.setStatus("COMPLETED");

        when(walletService.getMyTransactionHistory()).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/wallet/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(100.0));
    }

    @Test
    void testWalletResponseDtoCoverage() {
        WalletResponse response = new WalletResponse(1L, BigDecimal.TEN);
        org.junit.jupiter.api.Assertions.assertNotNull(response.getBalance());
    }
}