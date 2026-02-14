package com.ewallet.wallet_service.config;

import com.ewallet.wallet_service.entity.Admin;
import com.ewallet.wallet_service.repository.AdminRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @PostConstruct
    public void initAdmin() {

        if (adminRepository.count() == 0) {

            Admin admin = new Admin();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));

            adminRepository.save(admin);

            System.out.println("✅ Admin account created successfully.");
        }
    }
}
