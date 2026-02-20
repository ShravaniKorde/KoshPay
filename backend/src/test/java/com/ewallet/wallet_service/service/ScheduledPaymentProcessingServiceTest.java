package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.*;
import com.ewallet.wallet_service.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.mockito.Mockito.*;

class ScheduledPaymentProcessingServiceTest {
    private final WalletRepository walletRepo = mock(WalletRepository.class);
    private final ScheduledPaymentRepository schedRepo = mock(ScheduledPaymentRepository.class);
    private final AuditLogService audit = mock(AuditLogService.class);
    private final BalanceWebSocketService ws = mock(BalanceWebSocketService.class);
    private final TransactionStatusService status = mock(TransactionStatusService.class);
    
    private final ScheduledPaymentProcessingService service = new ScheduledPaymentProcessingService(
            walletRepo, schedRepo, mock(TransactionRepository.class), audit, ws, status);

    @Test
    void testExecute_SameWalletError() {
        User sender = new User(); sender.setId(1L);
        Wallet wallet = new Wallet(); wallet.setId(10L); wallet.setUser(sender);
        
        ScheduledPayment payment = new ScheduledPayment();
        payment.setSender(sender);
        payment.setReceiver(new VirtualPaymentAddress());
        payment.getReceiver().setUser(sender); // Same user

        when(walletRepo.findByUserId(1L)).thenReturn(Optional.of(wallet));
        
        service.executeSinglePayment(payment);
        verify(status, atLeastOnce()).updateStatus(any(), eq(TransactionStatus.FAILED));
    }
}