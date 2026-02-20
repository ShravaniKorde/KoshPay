package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.VirtualPaymentAddress;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QrServiceTest {
    private UserRepository userRepository;
    private VirtualPaymentAddressRepository vpaRepository;
    private QrService qrService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        vpaRepository = mock(VirtualPaymentAddressRepository.class);
        qrService = new QrService(userRepository, vpaRepository);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@koshpay.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void generateQrPayload_WithAmount() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        VirtualPaymentAddress vpa = new VirtualPaymentAddress();
        vpa.setUpiId("john@koshpay");
        vpa.setActive(true);

        when(userRepository.findByEmail("user@koshpay.com")).thenReturn(Optional.of(user));
        when(vpaRepository.findByUserId(1L)).thenReturn(Optional.of(vpa));

        String payload = qrService.generateQrPayload(new BigDecimal("100.00"));
        
        assertTrue(payload.contains("pa=john@koshpay"));
        assertTrue(payload.contains("pn=John+Doe"));
        assertTrue(payload.contains("am=100.00"));
    }
}