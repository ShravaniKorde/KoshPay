package com.ewallet.wallet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpResponse {
    private String status;     // e.g., "OTP_REQUIRED"
    private String message;    // e.g., "Please verify the OTP sent to your email"
    private String otp;        // The actual OTP code for the frontend to display
}
