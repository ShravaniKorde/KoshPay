package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.request.ContactCreateRequest;
import com.ewallet.wallet_service.entity.User;
import com.ewallet.wallet_service.entity.VirtualPaymentAddress;
import com.ewallet.wallet_service.repository.ContactRepository;
import com.ewallet.wallet_service.repository.UserRepository;
import com.ewallet.wallet_service.repository.VirtualPaymentAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContactServiceTest {
    private ContactRepository contactRepository;
    private UserRepository userRepository;
    private VirtualPaymentAddressRepository vpaRepository;
    private ContactService contactService;
    private User owner;

    @BeforeEach
    void setUp() {
        contactRepository = mock(ContactRepository.class);
        userRepository = mock(UserRepository.class);
        vpaRepository = mock(VirtualPaymentAddressRepository.class);
        contactService = new ContactService(contactRepository, userRepository, vpaRepository);

        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@koshpay.com");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("owner@koshpay.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail("owner@koshpay.com")).thenReturn(Optional.of(owner));
    }

    @Test
    void createContact_ShouldFail_WhenAddingSelf() {
        ContactCreateRequest request = new ContactCreateRequest();
        request.setUpiId("owner@koshpay");
        
        VirtualPaymentAddress selfVpa = new VirtualPaymentAddress();
        selfVpa.setUser(owner);
        when(vpaRepository.findByUpiId("owner@koshpay")).thenReturn(Optional.of(selfVpa));

        assertThrows(IllegalArgumentException.class, () -> contactService.createContact(request));
    }

    @Test
    void deleteContact_Success() {
        when(contactRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(new com.ewallet.wallet_service.entity.Contact()));
        
        assertDoesNotThrow(() -> contactService.deleteContact(10L));
        verify(contactRepository).delete(any());
    }

   @Test
    void testAddContact() {
        ContactCreateRequest req = new ContactCreateRequest();
        req.setUpiId("friend@koshpay");

        VirtualPaymentAddress friendVpa = new VirtualPaymentAddress();
        User friend = new User();
        friend.setId(2L);
        friendVpa.setUser(friend);

        when(vpaRepository.findByUpiId("friend@koshpay")).thenReturn(Optional.of(friendVpa));

        contactService.createContact(req);
        verify(contactRepository).save(any());
    }
}