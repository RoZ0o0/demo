package com.example.demo.exception;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(Long id) {
        super(String.format("Invoice with id '%s' does not exist.", id));
    }
}