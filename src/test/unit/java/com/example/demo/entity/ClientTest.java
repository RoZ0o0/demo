package com.example.demo.entity;

import com.example.demo.models.ClientRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
    }

    @Test
    void updateFromRequest_shouldUpdateAllFields() {
        ClientRequest clientRequest = new ClientRequest()
                .name("Client 1")
                .nip("123456789")
                .email("new@example.com")
                .phone("555-1234")
                .address("Street 1");

        client.updateFromRequest(clientRequest);

        assertEquals("Client 1", client.getName());
        assertEquals("123456789", client.getNip());
        assertEquals("new@example.com", client.getEmail());
        assertEquals("Street 1", client.getAddress());
        assertEquals("555-1234", client.getPhone());
    }

    @Test
    void updateFromRequest_shouldNormalizeNullOrEmptyFields() {
        ClientRequest clientRequest = new ClientRequest()
                .name("Client 1")
                .nip("123456789")
                .email(null)
                .phone(" ")
                .address("");

        client.updateFromRequest(clientRequest);

        assertEquals("Client 1", client.getName());
        assertEquals("123456789", client.getNip());
        assertNull(client.getEmail());
        assertNull(client.getAddress());
        assertNull(client.getPhone());
    }
}
