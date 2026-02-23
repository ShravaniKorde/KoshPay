package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.*;
import com.ewallet.wallet_service.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledPaymentProcessingServiceTest {

    @Mock private WalletRepository walletRepository;
    @Mock private ScheduledPaymentRepository scheduledPaymentRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private BalanceWebSocketService balanceWebSocketService;
    @Mock private TransactionStatusService statusService;

    @InjectMocks
    private ScheduledPaymentProcessingService service;

    private ScheduledPayment createPayment(BigDecimal senderBalance, BigDecimal amount) {

        User sender = new User();
        sender.setId(1L);

        Wallet senderWallet = new Wallet();
        senderWallet.setId(10L);
        senderWallet.setUser(sender);
        senderWallet.setBalance(senderBalance);

        User receiverUser = new User();
        receiverUser.setId(2L);

        Wallet receiverWallet = new Wallet();
        receiverWallet.setId(20L);
        receiverWallet.setUser(receiverUser);
        receiverWallet.setBalance(BigDecimal.ZERO);

        
        VirtualPaymentAddress vpa = new VirtualPaymentAddress();
        vpa.setUser(receiverUser);

        ScheduledPayment payment = new ScheduledPayment();
        payment.setSender(sender);
        payment.setReceiver(vpa); 
        payment.setAmount(amount);

        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(receiverWallet));

        return payment;
    }

    @Test
    void executeSinglePayment_shouldSucceed() {

        ScheduledPayment payment =
                createPayment(new BigDecimal("1000"), new BigDecimal("200"));

        service.executeSinglePayment(payment);

        assertTrue(payment.isExecuted());
        assertEquals(TransactionStatus.SUCCESS, payment.getStatus());

        verify(walletRepository, times(2)).save(any());
        verify(scheduledPaymentRepository).save(payment);
    }

    @Test
    void executeSinglePayment_shouldFail_whenInsufficientBalance() {

        ScheduledPayment payment =
                createPayment(new BigDecimal("100"), new BigDecimal("500"));

        service.executeSinglePayment(payment);

        assertEquals(TransactionStatus.FAILED, payment.getStatus());
        verify(scheduledPaymentRepository).save(payment);
    }
}