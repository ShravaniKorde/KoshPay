package com.ewallet.wallet_service.service.impl;

import com.ewallet.wallet_service.dto.request.LoginRequest;
import com.ewallet.wallet_service.dto.request.UserCreateRequest;
import com.ewallet.wallet_service.dto.response.AuthResponse;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.VirtualPaymentAddress;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.exception.InvalidRequestException;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.security.JwtUtil;
import com.ewallet.wallet_service.service.AuditLogService;
import com.ewallet.wallet_service.service.UserService;
import com.ewallet.wallet_service.service.util.UpiIdGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserServiceImpl.class);

    private static final BigDecimal MIN_INITIAL_BALANCE =
            BigDecimal.valueOf(1000);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final VirtualPaymentAddressRepository vpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    // =============================
    // CREATE USER + WALLET + UPI ID
    // =============================
    @Override
    @Transactional
    public void createUser(UserCreateRequest request) {

        log.info("User registration attempt for email={}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email already exists [{}]",
                    request.getEmail());
            throw new InvalidRequestException("Email already exists");
        }

       // 2. PIN Validation (Exactly 4 numeric digits)
        if (request.getTransactionPin() == null || !request.getTransactionPin().matches("\\d{4}")) {
            log.warn("Registration failed: Invalid PIN format for email={}", request.getEmail());
            throw new InvalidRequestException("Transaction PIN must be exactly 4 numeric digits");
        }

        if (request.getInitialBalance()
                .compareTo(MIN_INITIAL_BALANCE) < 0) {

            log.warn(
                "Registration failed for email={} due to low initial balance: {}",
                request.getEmail(),
                request.getInitialBalance()
            );

            throw new InvalidRequestException(
                    "Minimum initial balance must be ₹" + MIN_INITIAL_BALANCE
            );
        }

        // -------- ORIGINAL USER CREATION --------
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // HIGHLIGHT: Set the PIN here Encoded (Best for security)
        user.setTransactionPin(passwordEncoder.encode(request.getTransactionPin()));

        userRepository.save(user);

        // -------- ORIGINAL WALLET CREATION --------
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(request.getInitialBalance());

        walletRepository.save(wallet);

        // -------- NEW: UPI ID CREATION (EXTENSION ONLY) --------
        String base = UpiIdGenerator.generateBase(user.getName());
        int suffix = 0;
        String upiId;

        do {
            upiId = UpiIdGenerator.build(base, suffix++);
        } while (vpaRepository.existsByUpiId(upiId));

        VirtualPaymentAddress vpa = new VirtualPaymentAddress();
        vpa.setUpiId(upiId);
        vpa.setUser(user);

        vpaRepository.save(vpa);

        log.info(
            "User created successfully. userId={}, walletBalance={}, upiId={}",
            user.getId(),
            wallet.getBalance(),
            upiId
        );
    }

    // =============================
    // LOGIN (UNCHANGED)
    // =============================
    @Override
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for email={}", request.getEmail());

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        try {
            if (user == null) {
                log.warn("Login failed: Invalid email [{}]",
                        request.getEmail());
                throw new InvalidRequestException(
                        "Invalid email or password"
                );
            }

            if (!passwordEncoder.matches(
                    request.getPassword(),
                    user.getPassword())) {

                log.warn("Login failed: Wrong password for userId={}",
                        user.getId());

                auditLogService.log(
                        user,
                        "LOGIN",
                        "FAILURE",
                        null,
                        null
                );

                throw new InvalidRequestException(
                        "Invalid email or password"
                );
            }

            auditLogService.log(
                    user,
                    "LOGIN",
                    "SUCCESS",
                    null,
                    null
            );

            log.info("Login successful for userId={}", user.getId());

            String token = jwtUtil.generateToken(user.getEmail());
            return new AuthResponse(token);

        } catch (Exception e) {

            if (user != null &&
                !(e instanceof InvalidRequestException)) {

                log.error(
                    "Unexpected login error for userId={}",
                    user.getId(),
                    e
                );

                auditLogService.log(
                        user,
                        "LOGIN",
                        "FAILURE",
                        null,
                        null
                );
            }

            throw e;
        }
    }
}
