package com.example.demo.exception;

public class NipAlreadyExistsException extends RuntimeException{
    public NipAlreadyExistsException(String nip) {
        super(String.format("Client with NIP '%s' already exists", nip));
    }
}
