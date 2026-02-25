package com.ewallet.wallet_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt

    /**
     * Role values:
     *   ROLE_SUPER_ADMIN     → full access (all 4 tabs)
     *   ROLE_ANALYTICS       → Analytics tab only
     *   ROLE_TRANSACTIONS    → Transactions tab only
     *   ROLE_AUDIT_LOGS      → Audit Logs tab only
     */
    @Column(nullable = false)
    private String role = "ROLE_SUPER_ADMIN";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}