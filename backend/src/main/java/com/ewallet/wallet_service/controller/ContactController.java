package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.dto.request.ContactCreateRequest;
import com.ewallet.wallet_service.dto.response.ContactResponse;
import com.ewallet.wallet_service.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    // =============================
    // CREATE CONTACT
    // =============================
    @PostMapping
    public ResponseEntity<String> createContact(
            @Valid @RequestBody ContactCreateRequest request
    ) {
        contactService.createContact(request);
        return ResponseEntity.ok("Contact saved successfully");
    }

    // =============================
    // LIST MY CONTACTS
    // =============================
    @GetMapping
    public ResponseEntity<List<ContactResponse>> getMyContacts() {
        return ResponseEntity.ok(contactService.getMyContacts());
    }

    // =============================
    // DELETE CONTACT
    // =============================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContact(
            @PathVariable Long id
    ) {
        contactService.deleteContact(id);
        return ResponseEntity.ok("Contact deleted successfully");
    }
}
