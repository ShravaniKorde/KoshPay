package com.ewallet.wallet_service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApplicationTest {

    @Test
    void mainMethodTest() {
        assertDoesNotThrow(() -> {
            WalletServiceApplication.main(new String[]{"--server.port=0"});
        });
    }
}