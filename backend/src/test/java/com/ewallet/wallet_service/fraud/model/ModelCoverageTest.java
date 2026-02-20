package com.ewallet.wallet_service;

import com.ewallet.wallet_service.entity.*;
import com.ewallet.wallet_service.dto.request.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ModelCoverageTest {
    @Test
    void coverEntitiesAndDtos() {
        Admin admin = new Admin();
        admin.setEmail("admin@koshpay.com");
        assertEquals("admin@koshpay.com", admin.getEmail());

        UserCreateRequest request = new UserCreateRequest();
        request.setName("Test User");
        request.setInitialBalance(BigDecimal.TEN);
        assertEquals("Test User", request.getName());
        assertEquals(BigDecimal.TEN, request.getInitialBalance());
    }
}