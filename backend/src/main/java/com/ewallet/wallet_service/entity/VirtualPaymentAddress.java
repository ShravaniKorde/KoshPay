package com.ewallet.wallet_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "virtual_payment_addresses",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "upi_id"),
        @UniqueConstraint(columnNames = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class VirtualPaymentAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upi_id", nullable = false, unique = true)
    private String upiId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
