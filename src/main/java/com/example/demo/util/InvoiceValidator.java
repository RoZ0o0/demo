package com.example.demo.util;

import com.example.demo.exception.InvalidInvoiceDateException;
import com.example.demo.exception.InvalidInvoiceItemException;
import com.example.demo.models.InvoiceItemRequest;
import com.example.demo.models.InvoiceItemUpdateRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceValidator {
    private InvoiceValidator() {}

    private static final BigDecimal MAX_UNIT_PRICE = BigDecimal.valueOf(99999999.99);
    private static final long MAX_QUANTITY = 9999999999L;

    public static void validateUpdateItems(List<InvoiceItemUpdateRequest> items) {
        List<InvoiceItemRequest> invoiceItemRequests = items.stream()
                .map(i -> new InvoiceItemRequest()
                            .description(i.getDescription())
                            .quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice()))
                .toList();

        validateItems(invoiceItemRequests);
    }

    public static void validateItems(List<InvoiceItemRequest> items) {
        for (InvoiceItemRequest item : items) {
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
    }

    public static void validateDates(LocalDate issueDate, LocalDate dueDate) {
        if (issueDate.isAfter(dueDate)) {
            throw new InvalidInvoiceDateException("Issue date cannot be after due date");
        }
    }
}