package com.ewallet.wallet_service.service.util;

import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.transaction.annotation.Propagation; 
import org.springframework.transaction.annotation.Transactional; 

@Service
public class OtpService {
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private final UserRepository userRepository;

    public OtpService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateAndReturnOtp(User user) {
        String otp = String.valueOf(new Random().nextInt(9000) + 1000);
        user.setCurrentOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        return otp; 
    }

    @Transactional
    public boolean validateOtp(User user, String providedOtp) {
        if (user.getCurrentOtp() == null || user.getOtpExpiry() == null) {
            return false;
        }

        boolean isNotExpired = LocalDateTime.now().isBefore(user.getOtpExpiry());
        boolean matches = user.getCurrentOtp().equals(providedOtp);

        if (isNotExpired && matches) {
            user.setCurrentOtp(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
