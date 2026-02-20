package com.ewallet.wallet_service.controller;

import com.ewallet.wallet_service.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContactControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContactService contactService;

    @InjectMocks
    private ContactController contactController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(contactController).build();
    }

    @Test
    void testCreateContact() throws Exception {
        String json = "{\"name\":\"John Doe\", \"upiId\":\"john@upi\", \"email\":\"john@test.com\", \"phoneNumber\":\"9876543210\"}";
        
        doNothing().when(contactService).createContact(any());

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)); 
    }

    @Test
    void testGetMyContacts() throws Exception {
        mockMvc.perform(get("/api/contacts")).andExpect(status().isOk());
    }

    @Test
    void testDeleteContact() throws Exception {
        mockMvc.perform(delete("/api/contacts/1")).andExpect(status().isOk());
    }
}