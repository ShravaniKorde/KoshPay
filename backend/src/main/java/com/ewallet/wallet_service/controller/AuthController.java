package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.request.LoginRequest;
import com.ewallet.wallet_service.dto.response.AuthResponse;
import com.ewallet.wallet_service.entity.Admin;
import com.ewallet.wallet_service.exception.InvalidRequestException;
import com.ewallet.wallet_service.repository.AdminRepository;
import com.ewallet.wallet_service.security.JwtUtil;
import com.ewallet.wallet_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        if (request.isAdminLogin()) {

            Admin admin = adminRepository.findByEmail(request.getEmail())
                    .orElseThrow(() ->
                            new InvalidRequestException("Invalid admin credentials"));

            if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                throw new InvalidRequestException("Invalid admin credentials");
            }

            // ✅ Use the actual role stored in DB, not a hardcoded "ROLE_ADMIN"
            String token = jwtUtil.generateToken(admin.getEmail(), admin.getRole());
            return ResponseEntity.ok(new AuthResponse(token));
        }

        return ResponseEntity.ok(userService.login(request));
    }
}