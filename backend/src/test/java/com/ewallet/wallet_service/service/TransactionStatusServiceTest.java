package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.Transaction;
import com.ewallet.wallet_service.entity.TransactionStatus;
import com.ewallet.wallet_service.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionStatusServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionStatusService service;

    @Test
    void updateStatus_shouldSetStatusAndSave() {
        Transaction tx = new Transaction();

        service.updateStatus(tx, TransactionStatus.SUCCESS);

        verify(transactionRepository).saveAndFlush(tx);
    }
}
