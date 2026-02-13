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
    // ADMIN CREDENTIALS (FROM ENV)
    // =============================
    private final String adminEmail =
            System.getenv("ADMIN_EMAIL");

    private final String adminPasswordHash =
            System.getenv("ADMIN_PASSWORD_HASH");


    // =============================
    // CREATE USER + WALLET + UPI ID
    // =============================
    @Override
    @Transactional
    public void createUser(UserCreateRequest request) {

        log.info("User registration attempt for email={}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidRequestException("Email already exists");
        }

        if (request.getTransactionPin() == null ||
                !request.getTransactionPin().matches("\\d{4}")) {
            throw new InvalidRequestException(
                    "Transaction PIN must be exactly 4 numeric digits"
            );
        }

        if (request.getInitialBalance()
                .compareTo(MIN_INITIAL_BALANCE) < 0) {
            throw new InvalidRequestException(
                    "Minimum initial balance must be ₹" + MIN_INITIAL_BALANCE
            );
        }

        // -------- USER --------
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTransactionPin(
                passwordEncoder.encode(request.getTransactionPin())
        );

        userRepository.save(user);

        // -------- WALLET --------
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(request.getInitialBalance());
        walletRepository.save(wallet);

        // -------- UPI --------
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

        log.info("User created successfully: {}", user.getEmail());
    }


    // =============================
    // LOGIN (USER + ADMIN)
    // =============================
    @Override
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for email={}", request.getEmail());

        // =============================
        // ADMIN LOGIN (FIRST CHECK)
        // =============================
        if (adminEmail != null &&
                request.getEmail().equals(adminEmail)) {

            if (!passwordEncoder.matches(
                    request.getPassword(),
                    adminPasswordHash)) {

                throw new InvalidRequestException(
                        "Invalid admin credentials"
                );
            }

            log.info("Admin login successful");

            String token = jwtUtil.generateToken(
                    adminEmail,
                    "ROLE_ADMIN"
            );

            return new AuthResponse(token);
        }


        // =============================
        // NORMAL USER LOGIN
        // =============================
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (user == null ||
                !passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword())) {

            if (user != null) {
                auditLogService.log(
                        user,
                        "LOGIN",
                        "FAILURE",
                        null,
                        null
                );
            }

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

        log.info("User login successful: {}", user.getEmail());

        String token = jwtUtil.generateToken(
                user.getEmail(),
                "ROLE_USER"
        );

        return new AuthResponse(token);
    }
}
