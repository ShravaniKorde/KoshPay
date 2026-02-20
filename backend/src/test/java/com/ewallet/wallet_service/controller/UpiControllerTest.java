package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.VirtualPaymentAddress;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import com.ewallet.wallet_service.service.QrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpiControllerTest {

    private MockMvc mockMvc;
    @Mock private UserRepository userRepository;
    @Mock private VirtualPaymentAddressRepository vpaRepository;
    @Mock private QrService qrService;

    @InjectMocks private UpiController upiController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(upiController).build();
        
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@test.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetMyUpiId() throws Exception {
        User user = new User(); user.setId(1L);
        VirtualPaymentAddress vpa = new VirtualPaymentAddress(); vpa.setUpiId("test@koshpay");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(vpaRepository.findByUserId(1L)).thenReturn(Optional.of(vpa));

        mockMvc.perform(get("/api/upi/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upiId").value("test@koshpay"));
    }

    @Test
    void testGenerateQr() throws Exception {
        when(qrService.generateQrPayload(any())).thenReturn("qr_data");
        mockMvc.perform(get("/api/upi/qr").param("amount", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyUpiId_UserNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(java.util.Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/upi/me"));
        });

        assertTrue(exception.getCause() instanceof com.ewallet.wallet_service.exception.ResourceNotFoundException);
    }
}