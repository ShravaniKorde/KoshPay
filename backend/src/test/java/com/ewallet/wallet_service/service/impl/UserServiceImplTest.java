package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.request.LoginRequest;
import com.ewallet.wallet_service.dto.request.UserCreateRequest;
import com.ewallet.wallet_service.dto.response.AuthResponse;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.exception.InvalidRequestException;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.security.JwtUtil;
import com.ewallet.wallet_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_Success() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("new@example.com");
        request.setInitialBalance(new BigDecimal("1000"));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        userService.createUser(request);

        verify(userRepository).save(any(User.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createUser_EmailExists_ThrowsException() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("exists@example.com");
        when(userRepository.findByEmail("exists@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(InvalidRequestException.class, () -> userService.createUser(request));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("pass");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashed");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("token");

        AuthResponse response = userService.login(request);
        assertNotNull(response.getToken());
        verify(auditLogService).log(eq(user), eq("LOGIN"), eq("SUCCESS"), any(), any());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("none@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class, () -> userService.login(request));
        verify(auditLogService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void login_WrongPassword_LogsFailure() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong");

        User user = new User();
        user.setPassword("hashed");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidRequestException.class, () -> userService.login(request));
        verify(auditLogService).log(eq(user), eq("LOGIN"), eq("FAILURE"), any(), any());
    }
}