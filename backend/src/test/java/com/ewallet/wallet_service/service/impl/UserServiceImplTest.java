package com.ewallet.wallet_service.service.impl;

import com.ewallet.wallet_service.dto.request.LoginRequest;
import com.ewallet.wallet_service.dto.request.UserCreateRequest;
import com.ewallet.wallet_service.dto.response.AuthResponse;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.exception.InvalidRequestException;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.security.JwtUtil;
import com.ewallet.wallet_service.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private VirtualPaymentAddressRepository vpaRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(userService,
                "adminEmail", "admin@example.com");
        ReflectionTestUtils.setField(userService,
                "adminPasswordHash", "encodedAdmin");
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {

        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("test@example.com");
        request.setTransactionPin("1234");
        request.setInitialBalance(BigDecimal.valueOf(2000));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(InvalidRequestException.class,
                () -> userService.createUser(request));

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_shouldThrowException_whenInitialBalanceTooLow() {

        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("new@example.com");
        request.setTransactionPin("1234");
        request.setInitialBalance(BigDecimal.valueOf(500));

        when(userRepository.findByEmail("new@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> userService.createUser(request));
    }

    @Test
    void createUser_shouldThrowException_whenTransactionPinIsNull() {

        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("new@example.com");
        request.setTransactionPin(null);
        request.setInitialBalance(BigDecimal.valueOf(2000));

        when(userRepository.findByEmail("new@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> userService.createUser(request));
    }

    @Test
    void createUser_shouldThrowException_whenTransactionPinInvalid() {

        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("new@example.com");
        request.setTransactionPin("12ab");
        request.setInitialBalance(BigDecimal.valueOf(2000));

        when(userRepository.findByEmail("new@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> userService.createUser(request));
    }

    @Test
    void createUser_shouldCreateUserWalletAndVpa_whenValidRequest_loopExecutes() {

        UserCreateRequest request = new UserCreateRequest();
        request.setName("Test User");
        request.setEmail("valid@example.com");
        request.setPassword("password123");
        request.setTransactionPin("1234");
        request.setInitialBalance(BigDecimal.valueOf(2000));

        when(userRepository.findByEmail("valid@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(any()))
                .thenReturn("encoded");

        when(vpaRepository.existsByUpiId(any()))
                .thenReturn(true)
                .thenReturn(false);

        userService.createUser(request);

        verify(userRepository).save(any());
        verify(walletRepository).save(any());
        verify(vpaRepository).save(any());
    }

    @Test
    void createUser_shouldCreateUser_whenUpiAvailableImmediately() {

        UserCreateRequest request = new UserCreateRequest();
        request.setName("Another User");
        request.setEmail("another@example.com");
        request.setPassword("password123");
        request.setTransactionPin("1234");
        request.setInitialBalance(BigDecimal.valueOf(2000));

        when(userRepository.findByEmail("another@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(any()))
                .thenReturn("encoded");

        when(vpaRepository.existsByUpiId(any()))
                .thenReturn(false);

        userService.createUser(request);

        verify(userRepository).save(any());
        verify(walletRepository).save(any());
        verify(vpaRepository).save(any());
    }

    @Test
    void login_shouldReturnToken_whenUserCredentialsValid() {

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encoded");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encoded"))
                .thenReturn(true);

        when(jwtUtil.generateToken("user@example.com", "ROLE_USER"))
                .thenReturn("token");

        AuthResponse response = userService.login(request);

        assertEquals("token", response.getToken());

        verify(auditLogService).log(eq(user),
                eq("LOGIN"),
                eq("SUCCESS"),
                isNull(),
                isNull());
    }

    @Test
    void login_shouldThrowException_whenPasswordInvalid() {

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encoded");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "encoded"))
                .thenReturn(false);

        assertThrows(InvalidRequestException.class,
                () -> userService.login(request));

        verify(auditLogService).log(eq(user),
                eq("LOGIN"),
                eq("FAILURE"),
                isNull(),
                isNull());
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {

        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class,
                () -> userService.login(request));
    }

    @Test
    void login_shouldReturnToken_whenAdminValid() {

        LoginRequest request = new LoginRequest();
        request.setEmail("admin@example.com");
        request.setPassword("adminPass");

        when(passwordEncoder.matches("adminPass", "encodedAdmin"))
                .thenReturn(true);

        when(jwtUtil.generateToken("admin@example.com", "ROLE_ADMIN"))
                .thenReturn("adminToken");

        AuthResponse response = userService.login(request);

        assertEquals("adminToken", response.getToken());
    }

    @Test
    void login_shouldThrowException_whenAdminPasswordInvalid() {

        LoginRequest request = new LoginRequest();
        request.setEmail("admin@example.com");
        request.setPassword("wrongPassword");

        when(passwordEncoder.matches("wrongPassword", "encodedAdmin"))
                .thenReturn(false);

        assertThrows(InvalidRequestException.class,
                () -> userService.login(request));
    }

    @Test
    void login_shouldHandleNullAdminConfig() {
        ReflectionTestUtils.setField(userService, "adminEmail", null);
        
        LoginRequest request = new LoginRequest();
        request.setEmail("any@email.com");
        request.setPassword("password");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class, () -> userService.login(request));
    }

    @Test
    void login_shouldFail_whenUserExistsButPasswordMismatches() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong-pass");

        User user = new User();
        user.setPassword("correct-encoded-pass");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-pass", "correct-encoded-pass")).thenReturn(false);

        assertThrows(InvalidRequestException.class, () -> userService.login(request));
        verify(auditLogService).log(eq(user), eq("LOGIN"), eq("FAILURE"), any(), any());
    }

    @Test
    void createUser_shouldCoverPasswordEncoding() {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Encoder Test");
        request.setEmail("encode@test.com");
        request.setPassword("rawPass");
        request.setTransactionPin("9999");
        request.setInitialBalance(BigDecimal.valueOf(1500));

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPass")).thenReturn("encodedLoginPass");
        when(passwordEncoder.encode("9999")).thenReturn("encodedPin");
        when(vpaRepository.existsByUpiId(any())).thenReturn(false);

        userService.createUser(request);

        verify(passwordEncoder, times(2)).encode(any());
    }
}
