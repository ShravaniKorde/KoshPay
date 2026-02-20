package com.ewallet.wallet_service.security;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void testSecurityFilterChainFullCoverage() throws Exception {
        // 1. Setup mocks
        JwtFilter jwtFilter = mock(JwtFilter.class);
        CorsConfigurationSource corsSource = mock(CorsConfigurationSource.class);
        HttpSecurity http = mock(HttpSecurity.class);
        
        // 2. Prepare the mock chain return
        DefaultSecurityFilterChain mockChain = new DefaultSecurityFilterChain(
            mock(org.springframework.security.web.util.matcher.RequestMatcher.class), new ArrayList<>());
        when(http.build()).thenReturn(mockChain);

        when(http.cors(any())).thenReturn(http);
        when(http.csrf(any())).thenReturn(http);
        when(http.formLogin(any())).thenReturn(http);
        when(http.httpBasic(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);

        SecurityConfig config = new SecurityConfig(jwtFilter, corsSource);
        config.securityFilterChain(http);

        // Trigger CORS lambda
        ArgumentCaptor<org.springframework.security.config.Customizer<CorsConfigurer<HttpSecurity>>> corsCaptor = ArgumentCaptor.forClass(org.springframework.security.config.Customizer.class);
        verify(http).cors(corsCaptor.capture());
        corsCaptor.getValue().customize(mock(CorsConfigurer.class));

        ArgumentCaptor<org.springframework.security.config.Customizer<CsrfConfigurer<HttpSecurity>>> csrfCaptor = ArgumentCaptor.forClass(org.springframework.security.config.Customizer.class);
        verify(http).csrf(csrfCaptor.capture());
        csrfCaptor.getValue().customize(mock(CsrfConfigurer.class));

        ArgumentCaptor<org.springframework.security.config.Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>> authCaptor = ArgumentCaptor.forClass(org.springframework.security.config.Customizer.class);
        verify(http).authorizeHttpRequests(authCaptor.capture());
        authCaptor.getValue().customize(mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class, RETURNS_DEEP_STUBS));
    }
}