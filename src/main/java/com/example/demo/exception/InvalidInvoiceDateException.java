package com.example.demo.exception;

public class InvalidInvoiceDateException extends RuntimeException {
    public InvalidInvoiceDateException(String message) {
        super(message);
    }
}
