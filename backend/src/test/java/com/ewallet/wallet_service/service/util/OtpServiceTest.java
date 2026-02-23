package com.ewallet.wallet_service.service.util;

import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OtpServiceTest {
    private UserRepository userRepository;
    private OtpService otpService;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        otpService = new OtpService(userRepository);
        user = new User();
        user.setEmail("test@koshpay.com");
    }

    @Test
    void generateAndReturnOtp_ShouldSaveUser() {
        String otp = otpService.generateAndReturnOtp(user);
        assertNotNull(otp);
        assertEquals(4, otp.length());
        verify(userRepository).save(user);
    }

    @Test
    void validateOtp_Success() {
        user.setCurrentOtp("1234");
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        
        boolean isValid = otpService.validateOtp(user, "1234");
        
        assertTrue(isValid);
        assertNull(user.getCurrentOtp());
        verify(userRepository).save(user);
    }

    @Test
    void validateOtp_Expired() {
        user.setCurrentOtp("1234");
        user.setOtpExpiry(LocalDateTime.now().minusMinutes(1));
        
        assertFalse(otpService.validateOtp(user, "1234"));
    }

    @Test
    void validateOtp_WrongOtp() {
        user.setCurrentOtp("1234");
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        
        assertFalse(otpService.validateOtp(user, "9999"));
    }
}