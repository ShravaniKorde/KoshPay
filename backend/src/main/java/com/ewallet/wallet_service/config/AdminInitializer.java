package com.ewallet.wallet_service.config;

import com.ewallet.wallet_service.entity.Admin;
import com.ewallet.wallet_service.repository.AdminRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String superAdminEmail;

    @Value("${app.admin.password}")
    private String superAdminPassword;

    @PostConstruct
    public void initAdmins() {
        if (adminRepository.count() > 0) return;

        // ── 1. Super Admin (full access) ───────────────────────────
        Admin superAdmin = new Admin();
        superAdmin.setEmail(superAdminEmail);
        superAdmin.setPassword(passwordEncoder.encode(superAdminPassword));
        superAdmin.setRole("ROLE_SUPER_ADMIN");

        // ── 2. Analytics Admin ─────────────────────────────────────
        Admin analyticsAdmin = new Admin();
        analyticsAdmin.setEmail("analytics@koshpay.com");
        analyticsAdmin.setPassword(passwordEncoder.encode("analytics123"));
        analyticsAdmin.setRole("ROLE_ANALYTICS");

        // ── 3. Transactions Admin ──────────────────────────────────
        Admin txAdmin = new Admin();
        txAdmin.setEmail("transactions@koshpay.com");
        txAdmin.setPassword(passwordEncoder.encode("transactions123"));
        txAdmin.setRole("ROLE_TRANSACTIONS");

        // ── 4. Audit Logs Admin ────────────────────────────────────
        Admin auditAdmin = new Admin();
        auditAdmin.setEmail("auditlogs@koshpay.com");
        auditAdmin.setPassword(passwordEncoder.encode("auditlogs123"));
        auditAdmin.setRole("ROLE_AUDIT_LOGS");

        adminRepository.saveAll(List.of(superAdmin, analyticsAdmin, txAdmin, auditAdmin));

        System.out.println("✅ All admin accounts seeded successfully.");
        System.out.println("   Super Admin     → " + superAdminEmail);
        System.out.println("   Analytics Admin → analytics@koshpay.com");
        System.out.println("   Transactions    → transactions@koshpay.com");
        System.out.println("   Audit Logs      → auditlogs@koshpay.com");
    }
}