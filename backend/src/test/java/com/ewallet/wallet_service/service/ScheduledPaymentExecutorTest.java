package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.ScheduledPayment;
import com.ewallet.wallet_service.repository.ScheduledPaymentRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;

class ScheduledPaymentExecutorTest {
    @Test
    void testProcessLoop() {
        ScheduledPaymentRepository repo = mock(ScheduledPaymentRepository.class);
        ScheduledPaymentProcessingService proc = mock(ScheduledPaymentProcessingService.class);
        ScheduledPaymentExecutor executor = new ScheduledPaymentExecutor(repo, proc);

        when(repo.findPendingPayments(any())).thenReturn(List.of(new ScheduledPayment()));
        executor.processScheduledPayments();
        
        verify(proc, times(1)).executeSinglePayment(any());
    }
}