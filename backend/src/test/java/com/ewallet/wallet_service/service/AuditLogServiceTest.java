package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.AuditLog;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @InjectMocks private AuditLogService auditLogService;

    @Test
    void testLog_SavesToRepository() {
        User user = new User();
        user.setId(1L);
        
        // Matches your actual signature: User, String, String, BigDecimal, BigDecimal
        auditLogService.log(user, "TRANSFER", "SUCCESS", BigDecimal.ZERO, BigDecimal.ZERO);
        
        verify(auditLogRepository).save(any(AuditLog.class));
    }
}