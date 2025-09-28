package com.example.demo.exception;

public class InvoiceTotalExceededException extends RuntimeException {
    public InvoiceTotalExceededException(String message) {
        super(message);
    }
}
