package com.ewallet.wallet_service.service.impl;

import com.ewallet.wallet_service.dto.response.*;
import com.ewallet.wallet_service.entity.*;
import com.ewallet.wallet_service.exception.*;
import com.ewallet.wallet_service.fraud.model.*;
import com.ewallet.wallet_service.fraud.service.*;
import com.ewallet.wallet_service.repository.*;
import com.ewallet.wallet_service.service.*;
import com.ewallet.wallet_service.service.util.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceImplTest {

    private WalletRepository walletRepo;
    private TransactionRepository txRepo;
    private UserRepository userRepo;
    private VirtualPaymentAddressRepository vpaRepo;
    private BalanceWebSocketService wsService;
    private AuditLogService auditService;
    private FraudDetectionService fraudService;
    private TransactionStatusService statusService;
    private OtpService otpService;
    private PasswordEncoder encoder;
    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        walletRepo = mock(WalletRepository.class);
        txRepo = mock(TransactionRepository.class);
        userRepo = mock(UserRepository.class);
        vpaRepo = mock(VirtualPaymentAddressRepository.class);
        wsService = mock(BalanceWebSocketService.class);
        auditService = mock(AuditLogService.class);
        fraudService = mock(FraudDetectionService.class);
        statusService = mock(TransactionStatusService.class);
        otpService = mock(OtpService.class);
        encoder = mock(PasswordEncoder.class);

        walletService = new WalletServiceImpl(
                walletRepo, txRepo, userRepo, wsService, auditService,
                fraudService, statusService, otpService, encoder, vpaRepo
        );

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@test.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testTransfer_Success() {
        User senderUser = new User(); 
        senderUser.setId(1L); 
        senderUser.setTransactionPin("hash");
        
        Wallet sender = new Wallet(); 
        sender.setId(10L); 
        sender.setBalance(new BigDecimal("1000.00")); 
        sender.setUser(senderUser);
        
        Wallet receiver = new Wallet(); 
        receiver.setId(20L); 
        receiver.setBalance(new BigDecimal("500.00"));

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(senderUser));
        when(walletRepo.findByUserId(1L)).thenReturn(Optional.of(sender));
        when(walletRepo.findById(20L)).thenReturn(Optional.of(receiver));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        when(fraudService.evaluate(any())).thenReturn(new FraudResult(10, FraudDecision.ALLOW));

        Transaction mockTx = new Transaction();
        mockTx.setId(999L);
        when(txRepo.save(any(Transaction.class))).thenReturn(mockTx);

        Object result = walletService.transfer(20L, new BigDecimal("100.00"), "1234", null);

        assertEquals("SUCCESS", result);
    }

    @Test
    void testTransfer_InsufficientBalance() {
        User user = new User(); 
        user.setId(1L);
        Wallet sender = new Wallet(); 
        sender.setBalance(new BigDecimal("10.00"));
        sender.setUser(user);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(sender));

        assertThrows(IllegalArgumentException.class, () -> 
            walletService.transfer(2L, new BigDecimal("100.00"), "1234", null));
    }

    @Test
    void testGetMyBalance_UserNotFound() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            walletService.getMyBalance());
    }

    @Test
    void testUpdateTransactionPin_InvalidPinFormat() {
        User mockUser = new User();
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        assertThrows(IllegalArgumentException.class, () -> 
            walletService.updateTransactionPin("abc1")); 
            
        assertThrows(IllegalArgumentException.class, () -> 
            walletService.updateTransactionPin("123")); 
    }
}