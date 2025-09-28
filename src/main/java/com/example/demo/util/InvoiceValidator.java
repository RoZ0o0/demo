package com.example.demo.util;

import com.example.demo.exception.InvalidInvoiceDateException;
import com.example.demo.exception.InvalidInvoiceItemException;
import com.example.demo.exception.InvoiceTotalExceededException;
import com.example.demo.models.InvoiceItemRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceValidator {

    private static final BigDecimal MAX_UNIT_PRICE = BigDecimal.valueOf(99999999.99);
    private static final long MAX_QUANTITY = 9999999999L;

    public static void validateItem(InvoiceItemRequest item) {
        if (item.getQuantity() <= 0) {
            throw new InvalidInvoiceItemException("Quantity must be positive");
        }
        if (item.getQuantity() > MAX_QUANTITY) {
            throw new InvalidInvoiceItemException("Quantity too large");
        }
        BigDecimal unitPrice = new BigDecimal(item.getUnitPrice().toString());
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInvoiceItemException("Unit price cannot be negative");
        }
        if (unitPrice.compareTo(MAX_UNIT_PRICE) > 0) {
            throw new InvalidInvoiceItemException("Unit price too large");
        }
        if (unitPrice.scale() > 2) {
            throw new InvalidInvoiceItemException("Unit price must have at most 2 decimal places");
        }
        if (BigDecimal.valueOf(item.getQuantity()).scale() > 0) {
            throw new InvalidInvoiceItemException("Quantity must be a whole number");
        }
    }

    public static void validateTotals(BigDecimal net, BigDecimal vat, BigDecimal gross) {
        if (net.precision() - net.scale() > 10) {
            throw new InvoiceTotalExceededException("Net value exceeds maximum allowed digits");
        }
        if (vat.precision() - vat.scale() > 10) {
            throw new InvoiceTotalExceededException("VAT value exceeds maximum allowed digits");
        }
        if (gross.precision() - gross.scale() > 10) {
            throw new InvoiceTotalExceededException("Gross value exceeds maximum allowed digits");
        }
    }

    public static void validateDates(LocalDate issueDate, LocalDate dueDate) {
        if (issueDate.isAfter(dueDate)) {
            throw new InvalidInvoiceDateException("Issue date cannot be after due date");
        }
    }
}