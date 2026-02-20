package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.response.OtpResponse;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.service.UpiResolverService;
import com.ewallet.wallet_service.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UpiTransferControllerTest {

    private MockMvc mockMvc;
    @Mock private WalletService walletService;
    @Mock private UpiResolverService upiResolverService;

    @InjectMocks private UpiTransferController upiTransferController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(upiTransferController).build();
    }

    @Test
    void testTransferSuccess() throws Exception {
        Wallet wallet = new Wallet(); wallet.setId(1L);
        when(upiResolverService.resolveToWallet(any())).thenReturn(wallet);
        when(walletService.transfer(any(), any(), any(), any())).thenReturn("Success");

        String json = "{\"toUpiId\":\"dest@upi\", \"amount\":10, \"pin\":\"1234\"}";

        mockMvc.perform(post("/api/upi/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("UPI transfer successful"));
    }

  @Test
void testTransferRequiresOtp() throws Exception {
    Wallet wallet = new Wallet(); 
    wallet.setId(1L);
    when(upiResolverService.resolveToWallet(any())).thenReturn(wallet);
    
    OtpResponse mockOtp = new OtpResponse("123456", "Sent", "PENDING");
    when(walletService.transfer(any(), any(), any(), any())).thenReturn(mockOtp);

    String json = "{\"toUpiId\":\"dest@upi\", \"amount\":10, \"pin\":\"1234\"}";

    mockMvc.perform(post("/api/upi/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.*").exists()); 
}

@Test
void testTransferSuccess_StringResponse() throws Exception {
    Wallet wallet = new Wallet(); wallet.setId(1L);
    when(upiResolverService.resolveToWallet(any())).thenReturn(wallet);
    when(walletService.transfer(any(), any(), any(), any())).thenReturn("Success");

    String json = "{\"toUpiId\":\"dest@upi\", \"amount\":10, \"pin\":\"1234\"}";

    mockMvc.perform(post("/api/upi/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk())
            .andExpect(content().string("UPI transfer successful"));
}

@Test
void testTransferSuccess_OtpResponse() throws Exception {
    Wallet wallet = new Wallet(); wallet.setId(1L);
    when(upiResolverService.resolveToWallet(any())).thenReturn(wallet);
    
    OtpResponse otp = new OtpResponse("123456", "Pending", "Verify");
    when(walletService.transfer(any(), any(), any(), any())).thenReturn(otp);

    String json = "{\"toUpiId\":\"dest@upi\", \"amount\":10, \"pin\":\"1234\"}";

    mockMvc.perform(post("/api/upi/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.otp").exists()); 
}
}