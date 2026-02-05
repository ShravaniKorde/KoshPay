package com.ewallet.wallet_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");
        HttpServletRequest request = new MockHttpServletRequest();
        
        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void handleInsufficientBalance_Returns400() {
        InsufficientBalanceException ex = new InsufficientBalanceException("No funds");
        HttpServletRequest request = new MockHttpServletRequest();
        
        ResponseEntity<ApiErrorResponse> response = handler.handleInsufficientBalance(ex, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No funds", response.getBody().getMessage());
    }

    @Test
    void handleBadRequest_Returns400() {
        InvalidRequestException ex = new InvalidRequestException("Invalid data");
        HttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid data", response.getBody().getMessage());
    }
}