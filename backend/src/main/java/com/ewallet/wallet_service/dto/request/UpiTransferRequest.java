package com.ewallet.wallet_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import lombok.Data;

@Data 
public class UpiTransferRequest {

    @NotBlank
    private String toUpiId;

    @NotNull
    private BigDecimal amount;

    @NotBlank 
    private String pin; 
    private String otp; 
}
