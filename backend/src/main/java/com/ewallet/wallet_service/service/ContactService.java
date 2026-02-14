package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.request.ContactCreateRequest;
import com.ewallet.wallet_service.dto.response.ContactResponse;
import com.ewallet.wallet_service.entity.Contact;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.VirtualPaymentAddress;
import com.ewallet.wallet_service.exception.ResourceNotFoundException;
import com.ewallet.wallet_service.repository.ContactRepository;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final VirtualPaymentAddressRepository vpaRepository;

    public ContactService(
            ContactRepository contactRepository,
            UserRepository userRepository,
            VirtualPaymentAddressRepository vpaRepository
    ) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.vpaRepository = vpaRepository;
    }

    // =============================
    // CREATE CONTACT
    // =============================
    public void createContact(ContactCreateRequest request) {

        User owner = getCurrentUser();

        // Validate UPI exists in system
        VirtualPaymentAddress targetVpa = vpaRepository
                .findByUpiId(request.getUpiId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("UPI ID does not exist"));

        // Prevent adding yourself as contact
        if (targetVpa.getUser().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("You cannot add yourself as a contact");
        }

        Contact contact = new Contact();
        contact.setOwner(owner);
        contact.setDisplayName(request.getDisplayName());
        contact.setUpiId(request.getUpiId());

        contactRepository.save(contact);
    }

    // =============================
    // LIST CONTACTS
    // =============================
    @Transactional(readOnly = true)
    public List<ContactResponse> getMyContacts() {

        User owner = getCurrentUser();

        return contactRepository
                .findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .map(contact -> new ContactResponse(
                        contact.getId(),
                        contact.getDisplayName(),
                        contact.getUpiId(),
                        contact.getCreatedAt()
                ))
                .toList();
    }

    // =============================
    // DELETE CONTACT
    // =============================
    public void deleteContact(Long contactId) {

        User owner = getCurrentUser();

        Contact contact = contactRepository
                .findByIdAndOwnerId(contactId, owner.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Contact not found"));

        contactRepository.delete(contact);
    }

    // =============================
    // HELPER: CURRENT USER
    // =============================
    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
}
