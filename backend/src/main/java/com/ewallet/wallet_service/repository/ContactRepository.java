package com.ewallet.wallet_service.repository;

import com.ewallet.wallet_service.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    // Fetch all contacts of a user
    List<Contact> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    // Ensure deletion belongs to user
    Optional<Contact> findByIdAndOwnerId(Long id, Long ownerId);
}
