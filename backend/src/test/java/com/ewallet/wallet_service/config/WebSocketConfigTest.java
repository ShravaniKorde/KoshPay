package com.ewallet.wallet_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import com.ewallet.wallet_service.websocket.WebSocketConfig;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WebSocketConfigTest {

    @Test
    void testWebSocketConfiguration() {
        WebSocketConfig config = new WebSocketConfig();
        
        StompEndpointRegistry endpointRegistry = mock(StompEndpointRegistry.class);
        
        StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);
        
        when(endpointRegistry.addEndpoint(anyString())).thenReturn(registration);
        when(registration.setAllowedOriginPatterns(anyString())).thenReturn(registration);
        
        MessageBrokerRegistry brokerRegistry = mock(MessageBrokerRegistry.class);

        config.registerStompEndpoints(endpointRegistry);
        config.configureMessageBroker(brokerRegistry);

        verify(endpointRegistry).addEndpoint("/ws");
        verify(brokerRegistry).enableSimpleBroker("/topic");
        verify(brokerRegistry).setApplicationDestinationPrefixes("/app");
    }
}