package com.ewallet.wallet_service.dto;

import com.ewallet.wallet_service.exception.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DtoTest {

    @Test
    void testApiErrorResponse() {
        LocalDateTime now = LocalDateTime.now();
        ApiErrorResponse error = new ApiErrorResponse(now, 404, "Not Found", "User missing", "/api/test");
        
        assertEquals(404, error.getStatus());
        assertEquals("User missing", error.getMessage());
        assertEquals(now, error.getTimestamp());
    }
}