package com.ewallet.wallet_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void testExceptionClasses() {
        assertNotNull(new ResourceNotFoundException("Not Found").getMessage());
        assertNotNull(new InvalidRequestException("Invalid").getMessage());
        assertNotNull(new InsufficientBalanceException("Low Balance").getMessage());

        ApiErrorResponse error = new ApiErrorResponse(
            LocalDateTime.now(), 404, "Not Found", "Msg", "/path"
        );
        assertEquals(404, error.getStatus());
        error.setMessage("New Msg");
        assertEquals("New Msg", error.getMessage());
    }

    @Test
    void testGlobalHandlerDirectly() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        ResponseEntity<ApiErrorResponse> res1 = handler.handleNotFound(
            new ResourceNotFoundException("User not found"), request
        );
        assertEquals(HttpStatus.NOT_FOUND, res1.getStatusCode());

        ResponseEntity<ApiErrorResponse> res2 = handler.handleBusinessExceptions(
            new InsufficientBalanceException("No money"), request
        );
        assertEquals(HttpStatus.BAD_REQUEST, res2.getStatusCode());

        ResponseEntity<ApiErrorResponse> res3 = handler.handleGeneric(
            new RuntimeException("System crash"), request
        );
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res3.getStatusCode());
    }
}