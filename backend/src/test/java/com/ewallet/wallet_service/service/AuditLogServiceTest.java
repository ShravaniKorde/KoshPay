package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.entity.AuditLog;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;

class AuditLogServiceTest {
    private final AuditLogRepository repository = mock(AuditLogRepository.class);
    private final AuditLogService service = new AuditLogService(repository);

    @Test
    void testLogWithUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@koshpay.com");
        service.log(user, "LOGIN", "SUCCESS", BigDecimal.ZERO, BigDecimal.ZERO);
        verify(repository).save(any(AuditLog.class));
    }

    @Test
    void testLogWithNullUser() {
        service.log(null, "LOGIN", "SUCCESS", BigDecimal.ZERO, BigDecimal.ZERO);
        verify(repository).save(any(AuditLog.class));
    }

    @Test
    void testLogExceptionHandling() {
        doThrow(new RuntimeException("DB Down")).when(repository).save(any());
        service.log(null, "LOGIN", "SUCCESS", BigDecimal.ZERO, BigDecimal.ZERO);
    }
}