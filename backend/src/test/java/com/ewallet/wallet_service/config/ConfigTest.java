package com.ewallet.wallet_service.config;

import com.ewallet.wallet_service.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigTest {

    @Test
    void testSecurityBeansConfig() throws Exception {
        SecurityBeansConfig config = new SecurityBeansConfig();
        
        assertNotNull(config.passwordEncoder());
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        config.authenticationManager(authConfig);
        verify(authConfig, times(1)).getAuthenticationManager();
    }

    @Test
    void testCorsConfig() {
        CorsConfig config = new CorsConfig();
        CorsConfigurationSource source = config.corsConfigurationSource();
        assertNotNull(source);
    }

    @Test
    void testOpenApiConfig() {
        OpenApiConfig config = new OpenApiConfig();
        assertNotNull(config);
    }

    @Test
    void testAdminInitializer() {
        AdminRepository adminRepo = mock(AdminRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        
        AdminInitializer initializer = new AdminInitializer(adminRepo, encoder);
        
        ReflectionTestUtils.setField(initializer, "adminEmail", "admin@test.com");
        ReflectionTestUtils.setField(initializer, "adminPassword", "password");

        when(adminRepo.count()).thenReturn(1L);
        initializer.initAdmin();
        verify(adminRepo, never()).save(any());

        when(adminRepo.count()).thenReturn(0L);
        when(encoder.encode(anyString())).thenReturn("hashed_pass");
        initializer.initAdmin();
        verify(adminRepo, times(1)).save(any());
    }
}