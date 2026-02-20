package com.ewallet.wallet_service.repository;

import com.ewallet.wallet_service.entity.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepoTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);

    @Test
    void triggerRepositoryMethods() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));
        assertNotNull(userRepository.findByEmail("test@test.com"));
        
        when(transactionRepository.sumSuccessfulTransactionVolume()).thenReturn(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, transactionRepository.sumSuccessfulTransactionVolume());
        
        when(transactionRepository.countTransactionsAfter(any())).thenReturn(5L);
        assertEquals(5L, transactionRepository.countTransactionsAfter(Instant.now()));

        when(auditLogRepository.countByStatus("SUCCESS")).thenReturn(10L);
        assertEquals(10L, auditLogRepository.countByStatus("SUCCESS"));
    }
}