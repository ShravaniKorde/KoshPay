package com.ewallet.wallet_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class WalletServiceApplicationTests {

    @Test
    void testApplicationStartupAndConstructor() {
        WalletServiceApplication app = new WalletServiceApplication();
        assertNotNull(app);

        try (var mockedSpringApp = mockStatic(SpringApplication.class)) {
            mockedSpringApp.when(() -> SpringApplication.run(eq(WalletServiceApplication.class), any(String[].class)))
                    .thenReturn(org.mockito.Mockito.mock(ConfigurableApplicationContext.class));

            WalletServiceApplication.main(new String[]{});
            
            mockedSpringApp.verify(() -> SpringApplication.run(eq(WalletServiceApplication.class), any(String[].class)));
        }
    }
}