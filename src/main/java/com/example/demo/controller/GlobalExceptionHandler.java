package com.example.demo.controller;

import com.example.demo.exception.InvalidInvoiceDateException;
import com.example.demo.exception.InvalidInvoiceItemException;
import com.example.demo.exception.InvoiceNumberExistsException;
import com.example.demo.exception.InvoiceTotalExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvoiceNumberExistsException.class)
    public ResponseEntity<Map<String, String>> handleNumberInvoiceExistsException(InvoiceNumberExistsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler({InvalidInvoiceItemException.class, InvoiceTotalExceededException.class, InvalidInvoiceDateException.class})
    public ResponseEntity<Map<String, String>> handleInvoiceValidation(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
