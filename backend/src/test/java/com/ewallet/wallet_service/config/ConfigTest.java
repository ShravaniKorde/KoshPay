package com.ewallet.wallet_service.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigTest {

    @Test
    void testCorsConfig() {
        CorsConfig config = new CorsConfig();
        assertNotNull(config.corsFilter());
    }

    @Test
    void testOpenApiConfig() {
        OpenApiConfig config = new OpenApiConfig();
        assertNotNull(config);
    }

    @Test
    void testSecurityBeans() {
        SecurityBeansConfig config = new SecurityBeansConfig();
        assertNotNull(config.passwordEncoder());
    }
}