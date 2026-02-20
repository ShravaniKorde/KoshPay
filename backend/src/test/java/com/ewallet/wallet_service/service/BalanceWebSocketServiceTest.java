package com.ewallet.wallet_service.service;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;

class BalanceWebSocketServiceTest {
    @Test
    void testPublishBalance() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        BalanceWebSocketService service = new BalanceWebSocketService(template);
        service.publishBalance(100L, BigDecimal.valueOf(500.50));
        verify(template).convertAndSend(eq("/topic/wallet/100"), any(Object.class));
    }
}