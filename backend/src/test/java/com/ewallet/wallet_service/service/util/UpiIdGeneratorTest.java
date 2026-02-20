package com.ewallet.wallet_service.service.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UpiIdGeneratorTest {

    @Test
    void generateBase_ShouldRemoveSpecialCharactersAndSpaces() {
        assertEquals("johnwick", UpiIdGenerator.generateBase("John Wick 007!"));
        assertEquals("testuser", UpiIdGenerator.generateBase("Test-User_123"));
    }

    @Test
    void build_WithZeroSuffix_ShouldNotIncludeNumber() {
        String upi = UpiIdGenerator.build("johnwick", 0);
        assertEquals("johnwick@koshpay", upi);
    }

    @Test
    void build_WithPositiveSuffix_ShouldIncludeNumber() {
        String upi = UpiIdGenerator.build("johnwick", 1);
        assertEquals("johnwick1@koshpay", upi);
    }
}