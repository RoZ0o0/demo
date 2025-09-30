package com.example.demo.exception;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(Long clientId) {
        super(String.format("Client with id '%s' does not exist.", clientId));
    }
}