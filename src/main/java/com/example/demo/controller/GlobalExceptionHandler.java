package com.example.demo.controller;

import com.example.demo.exception.InvalidInvoiceDateException;
import com.example.demo.exception.InvalidInvoiceItemException;
import com.example.demo.exception.InvoiceNumberExistsException;
import com.example.demo.exception.InvoiceTotalExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_KEY = "error";

    @ExceptionHandler(InvoiceNumberExistsException.class)
    public ResponseEntity<Map<String, String>> handleNumberInvoiceExistsException(InvoiceNumberExistsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_KEY, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler({InvalidInvoiceItemException.class, InvoiceTotalExceededException.class, InvalidInvoiceDateException.class})
    public ResponseEntity<Map<String, String>> handleInvoiceValidation(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_KEY, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_KEY, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
