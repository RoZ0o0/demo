package com.example.demo.exception;

public class InvalidInvoiceItemException extends RuntimeException{
    public InvalidInvoiceItemException(String message) {
        super(message);
    }
}
