package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.response.WalletResponse;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.repository.*;
import com.ewallet.wallet_service.service.impl.WalletServiceImpl;
import com.ewallet.wallet_service.service.util.OtpService;
import com.ewallet.wallet_service.fraud.service.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    @Mock private WalletRepository walletRepository;
    @Mock private UserRepository userRepository;
    @Mock private TransactionRepository txRepo;
    @Mock private VirtualPaymentAddressRepository vpaRepo;
    @Mock private BalanceWebSocketService wsService;
    @Mock private AuditLogService auditService;
    @Mock private FraudDetectionService fraudService;
    @Mock private TransactionStatusService statusService;
    @Mock private OtpService otpService;
    @Mock private PasswordEncoder encoder;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@koshpay.com");
    }

    @Test
    void testTransfer_ReceiverNotFound_ThrowsException() {
        User senderUser = new User(); senderUser.setTransactionPin("hash");
        Wallet senderWallet = new Wallet(); senderWallet.setUser(senderUser);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(senderUser));
        when(walletRepository.findByUserId(any())).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            walletService.transfer(999L, BigDecimal.TEN, "1234", null);
        });
    }

   @Test
    void testGetMyBalance_Success() {
   
        User user = new User();
        user.setId(1L);
        user.setEmail("test@koshpay.com");
    
        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("1500.00"));
        wallet.setUser(user);

        when(userRepository.findByEmail("test@koshpay.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.getMyBalance();

        assertNotNull(response);
        assertEquals(new BigDecimal("1500.00"), response.getBalance());
    }
}