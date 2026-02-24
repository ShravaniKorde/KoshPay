package com.ewallet.wallet_service.service.impl;

import com.ewallet.wallet_service.dto.response.OtpResponse;
import com.ewallet.wallet_service.dto.response.TransactionResponse;
import com.ewallet.wallet_service.dto.response.WalletResponse;
import com.ewallet.wallet_service.entity.*;
import com.ewallet.wallet_service.exception.*;
import com.ewallet.wallet_service.fraud.model.FraudResult;
import com.ewallet.wallet_service.fraud.service.FraudDecision;
import com.ewallet.wallet_service.fraud.service.FraudDetectionService;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(context.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("user@test.com");
        SecurityContextHolder.setContext(context);
    }

    // ===================== BALANCE =====================

    @Test
    void testGetMyBalance_Success() {
        User user = new User();
        user.setId(1L);

        Wallet wallet = new Wallet();
        wallet.setId(10L);
        wallet.setBalance(new BigDecimal("500"));
        wallet.setUser(user);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(1L)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.getMyBalance();

        assertEquals(new BigDecimal("500"), response.getBalance());
    }

    @Test
    void testGetMyBalance_UserNotFound() {
        when(userRepo.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.getMyBalance());
    }

    // ===================== PIN =====================

    @Test
    void testUpdateTransactionPin_Success() {
        User user = new User();
        user.setId(1L);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(encoder.encode("1234")).thenReturn("encodedPin");

        walletService.updateTransactionPin("1234");

        verify(userRepo).save(user);
        assertEquals("encodedPin", user.getTransactionPin());
    }

    @Test
    void testUpdateTransactionPin_InvalidFormat() {
        User user = new User();
        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> walletService.updateTransactionPin("abc1"));
    }

    // ===================== TRANSFER =====================

    @Test
    void testTransfer_InvalidPin() {
        User user = new User();
        user.setId(1L);
        user.setTransactionPin("hash");

        Wallet sender = new Wallet();
        sender.setId(10L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("1000"));

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(sender));
        when(encoder.matches(any(), any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> walletService.transfer(20L, new BigDecimal("100"), "1234", null));
    }

    @Test
    void testTransfer_SelfTransfer() {
        User user = new User();
        user.setId(1L);
        user.setTransactionPin("hash");

        Wallet sender = new Wallet();
        sender.setId(10L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("1000"));

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(sender));
        when(encoder.matches(any(), any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> walletService.transfer(10L, new BigDecimal("100"), "1234", null));
    }

    @Test
    void testTransfer_InsufficientBalance() {
        User user = new User();
        user.setId(1L);
        user.setTransactionPin("hash");

        Wallet sender = new Wallet();
        sender.setId(10L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("10"));

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(sender));
        when(encoder.matches(any(), any())).thenReturn(true);

        assertThrows(InsufficientBalanceException.class,
                () -> walletService.transfer(20L, new BigDecimal("100"), "1234", null));
    }

    @Test
    void testTransfer_FraudBlocked() {
        User user = new User();
        user.setId(1L);
        user.setTransactionPin("hash");

        Wallet sender = new Wallet();
        sender.setId(10L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("1000"));

        Wallet receiver = new Wallet();
        receiver.setId(20L);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(sender));
        when(walletRepo.findById(20L)).thenReturn(Optional.of(receiver));
        when(encoder.matches(any(), any())).thenReturn(true);
        when(fraudService.evaluate(any()))
                .thenReturn(new FraudResult(90, FraudDecision.BLOCK));

        assertThrows(InvalidRequestException.class,
                () -> walletService.transfer(20L, new BigDecimal("100"), "1234", null));
    }

    @Test
    void testTransfer_OtpRequired() {
        User user = new User();
        user.setId(1L);
        user.setTransactionPin("hash");

        Wallet sender = new Wallet();
        sender.setId(10L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("5000"));

        Wallet receiver = new Wallet();
        receiver.setId(20L);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(sender));
        when(walletRepo.findById(20L)).thenReturn(Optional.of(receiver));
        when(encoder.matches(any(), any())).thenReturn(true);
        when(fraudService.evaluate(any()))
                .thenReturn(new FraudResult(60, FraudDecision.ALLOW));
        when(otpService.generateAndReturnOtp(any())).thenReturn("123456");

        Object result = walletService.transfer(20L, new BigDecimal("2000"), "1234", null);

        assertTrue(result instanceof OtpResponse);
    }

    // ===================== HISTORY =====================

    @Test
    void testGetMyTransactionHistory_Success() {
        User user = new User();
        user.setId(1L);
        user.setName("John");

        Wallet wallet = new Wallet();
        wallet.setId(10L);
        wallet.setUser(user);

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setAmount(new BigDecimal("100"));
        tx.setFromWallet(wallet);
        tx.setToWallet(wallet);
        tx.setTimestamp(Instant.now());
        tx.setStatus(TransactionStatus.SUCCESS);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(txRepo.findByFromWalletIdOrToWalletIdOrderByTimestampDesc(10L, 10L))
                .thenReturn(List.of(tx));

        List<TransactionResponse> history = walletService.getMyTransactionHistory();

        assertEquals(1, history.size());
    }

    @Test
    void testTransfer_OtpInvalid() {
        User user = new User();
        user.setId(1L);
        user.setTransactionPin("hash");

        Wallet sender = new Wallet();
        sender.setId(10L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("5000"));

        Wallet receiver = new Wallet();
        receiver.setId(20L);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(sender));
        when(walletRepo.findById(20L)).thenReturn(Optional.of(receiver));
        when(encoder.matches(any(), any())).thenReturn(true);

        when(fraudService.evaluate(any()))
                .thenReturn(new FraudResult(60, FraudDecision.ALLOW));

        when(otpService.validateOtp(any(), any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> walletService.transfer(20L,
                        new BigDecimal("2000"),
                        "1234",
                        "wrongOtp"));
    }

    @Test
    void testTransfer_OtpValid_Success() {
        User user = new User();
        user.setId(1L);
        user.setTransactionPin("hash");

        Wallet sender = new Wallet();
        sender.setId(10L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("5000"));

        Wallet receiver = new Wallet();
        receiver.setId(20L);
        receiver.setBalance(BigDecimal.ZERO);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(sender));
        when(walletRepo.findById(20L)).thenReturn(Optional.of(receiver));
        when(encoder.matches(any(), any())).thenReturn(true);

        when(fraudService.evaluate(any()))
                .thenReturn(new FraudResult(60, FraudDecision.ALLOW));

        when(otpService.validateOtp(any(), any())).thenReturn(true);

        Object result = walletService.transfer(20L,
                new BigDecimal("2000"),
                "1234",
                "123456");

        assertEquals("SUCCESS", result);
    }

    @Test
    void testTransfer_ReceiverNotFound() {
        User user = new User();
        user.setId(1L);
        user.setTransactionPin("hash");

        Wallet sender = new Wallet();
        sender.setId(10L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("1000"));

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(sender));
        when(encoder.matches(any(), any())).thenReturn(true);
        when(walletRepo.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.transfer(20L,
                        new BigDecimal("100"),
                        "1234",
                        null));
    }

    @Test
    void testGetMyBalance_WalletNotFound() {
        User user = new User();
        user.setId(1L);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> walletService.getMyBalance());
    }

    @Test
    void testTransactionHistory_NullNameFallback() {
        User user = new User();
        user.setId(1L);
        user.setName(null);

        Wallet wallet = new Wallet();
        wallet.setId(10L);
        wallet.setUser(user);

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setAmount(BigDecimal.TEN);
        tx.setFromWallet(wallet);
        tx.setToWallet(wallet);
        tx.setTimestamp(Instant.now());
        tx.setStatus(TransactionStatus.SUCCESS);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(wallet));
        when(txRepo.findByFromWalletIdOrToWalletIdOrderByTimestampDesc(any(), any()))
                   .thenReturn(List.of(tx));
        when(vpaRepo.findByUserId(any())).thenReturn(Optional.empty());

        List<TransactionResponse> result =
                walletService.getMyTransactionHistory();
        assertEquals("user@koshpay", result.get(0).getFromUpi());
    }


    @Test
    void testTransfer_SmallAmount_NoOtp_Success() {
        User user = new User();
        user.setId(1L);
        user.setTransactionPin("hash");

        Wallet sender = new Wallet();
        sender.setId(10L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("1000"));

        Wallet receiver = new Wallet();
        receiver.setId(20L);
        receiver.setBalance(BigDecimal.ZERO);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(sender));
        when(walletRepo.findById(any())).thenReturn(Optional.of(receiver));
        when(encoder.matches(any(), any())).thenReturn(true);

        when(fraudService.evaluate(any()))
                .thenReturn(new FraudResult(20, FraudDecision.ALLOW));

        Object result = walletService.transfer(
                20L,
                new BigDecimal("500"),
                "1234",
                null
        );

        assertEquals("SUCCESS", result);
    }

    @Test
    void testTransactionHistory_NameFormatting() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");

        Wallet wallet = new Wallet();
        wallet.setId(10L);
        wallet.setUser(user);

        Transaction tx = new Transaction();
        tx.setId(1L);
        tx.setAmount(BigDecimal.TEN);
        tx.setFromWallet(wallet);
        tx.setToWallet(wallet);
        tx.setTimestamp(Instant.now());
        tx.setStatus(TransactionStatus.SUCCESS);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(wallet));
        when(txRepo.findByFromWalletIdOrToWalletIdOrderByTimestampDesc(any(), any()))
                   .thenReturn(List.of(tx));
        when(vpaRepo.findByUserId(any())).thenReturn(Optional.empty());

        List<TransactionResponse> result =
                walletService.getMyTransactionHistory();

        assertEquals("johndoe@koshpay", result.get(0).getFromUpi());
    }

    @Test
    void testTransfer_ExceptionDuringSave_ShouldMarkFailed() {
        User user = new User();
        user.setId(1L);
        user.setTransactionPin("hash");

        Wallet sender = new Wallet();
        sender.setId(10L);
        sender.setUser(user);
        sender.setBalance(new BigDecimal("1000"));

        Wallet receiver = new Wallet();
        receiver.setId(20L);
        receiver.setBalance(BigDecimal.ZERO);

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(walletRepo.findByUserId(any())).thenReturn(Optional.of(sender));
        when(walletRepo.findById(any())).thenReturn(Optional.of(receiver));
        when(encoder.matches(any(), any())).thenReturn(true);

        when(fraudService.evaluate(any()))
                .thenReturn(new FraudResult(20, FraudDecision.ALLOW));

        doThrow(new RuntimeException("DB error"))
                .when(walletRepo).save(sender);

        assertThrows(RuntimeException.class,
                () -> walletService.transfer(
                        20L,
                        new BigDecimal("100"),
                        "1234",
                        null
                ));
    }
}
