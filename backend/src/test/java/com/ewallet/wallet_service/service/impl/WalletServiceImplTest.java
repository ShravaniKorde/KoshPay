package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.response.TransactionResponse;
import com.ewallet.wallet_service.dto.response.WalletResponse;
import com.ewallet.wallet_service.entity.Transaction;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.exception.InsufficientBalanceException;
import com.ewallet.wallet_service.exception.ResourceNotFoundException;
import com.ewallet.wallet_service.repository.TransactionRepository;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private BalanceWebSocketService balanceWebSocketService;
    @Mock private AuditLogService auditLogService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal("1000.00"));
    }

    private void mockCurrentUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
    }

    @Test
    void getMyBalance_Success() {
        mockCurrentUser();
        WalletResponse response = walletService.getMyBalance();
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
    }

    @Test
    void transfer_Success() {
        mockCurrentUser();
        Wallet target = new Wallet();
        target.setId(2L);
        target.setUser(new User());
        target.setBalance(new BigDecimal("500.00"));

        when(walletRepository.findById(2L)).thenReturn(Optional.of(target));
        
        walletService.transfer(2L, new BigDecimal("200.00"));

        assertEquals(new BigDecimal("800.00"), wallet.getBalance());
        verify(walletRepository, times(2)).save(any());
        verify(auditLogService, times(2)).log(any(), anyString(), eq("SUCCESS"), any(), any());
    }

    @Test
    void transfer_ToSelf_ThrowsException() {
        mockCurrentUser();
        // The code looks for target wallet first. We must mock it finding itself.
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class, () -> 
            walletService.transfer(1L, new BigDecimal("100.00"))
        );
    }

    @Test
    void transfer_NegativeAmount_ThrowsException() {
        mockCurrentUser();
        Wallet target = new Wallet();
        target.setId(2L);
        // The code looks for target wallet first. We must mock it.
        when(walletRepository.findById(2L)).thenReturn(Optional.of(target));

        assertThrows(IllegalArgumentException.class, () -> 
            walletService.transfer(2L, new BigDecimal("-50.00"))
        );
    }

    @Test
    void transfer_TargetNotFound_ThrowsException() {
        mockCurrentUser();
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            walletService.transfer(99L, new BigDecimal("10.00"))
        );
    }

    @Test
    void transfer_InsufficientBalance_LogsFailure() {
        mockCurrentUser();
        Wallet target = new Wallet();
        target.setId(2L);
        when(walletRepository.findById(2L)).thenReturn(Optional.of(target));

        assertThrows(InsufficientBalanceException.class, () -> 
            walletService.transfer(2L, new BigDecimal("5000.00"))
        );
        verify(auditLogService).log(eq(user), eq("TRANSFER"), eq("FAILURE"), any(), any());
    }

    @Test
    void getMyTransactionHistory_Success() {
        mockCurrentUser();
        Transaction tx = new Transaction();
        tx.setFromWallet(wallet);
        tx.setToWallet(new Wallet());
        tx.getToWallet().setId(2L);
        tx.setAmount(BigDecimal.TEN);
        tx.setTimestamp(LocalDateTime.now());

        when(transactionRepository.findByFromWalletIdOrToWalletIdOrderByTimestampDesc(1L, 1L))
                .thenReturn(List.of(tx));

        List<TransactionResponse> history = walletService.getMyTransactionHistory();
        assertFalse(history.isEmpty());
        assertEquals("DEBIT", history.get(0).getType());
    }
}