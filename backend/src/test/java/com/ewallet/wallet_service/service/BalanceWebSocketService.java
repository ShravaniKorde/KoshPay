package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.response.BalanceUpdateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BalanceWebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BalanceWebSocketService balanceWebSocketService;

    @Test
    void publishBalance_SendsMessage() {
        Long walletId = 1L;
        BigDecimal balance = new BigDecimal("100.00");
        
        balanceWebSocketService.publishBalance(walletId, balance);
        
        // Match the destination exactly as defined in the Service class: "/topic/wallet/"
        verify(messagingTemplate).convertAndSend(
            eq("/topic/wallet/" + walletId), 
            any(BalanceUpdateResponse.class)
        );
    }
}