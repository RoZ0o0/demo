package com.example.demo.exception;

public class InvoiceNumberExistsException extends RuntimeException {
    public InvoiceNumberExistsException(String invoiceNumber) {
        super(String.format("Invoice with invoice number '%s' already exists.", invoiceNumber));
    }
}
