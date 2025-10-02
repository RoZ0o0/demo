package com.example.demo.util;

import com.example.demo.exception.InvalidInvoiceDateException;
import com.example.demo.exception.InvalidInvoiceItemException;
import com.example.demo.models.InvoiceItemRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceValidatorTest {

    @Test
    void validateItems_shouldPassForValidItems() {
        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item 1")
                .quantity(10L)
                .unitPrice(100.0);

        assertDoesNotThrow(() -> InvoiceValidator.validateItems(List.of(invoiceItemRequest)));
    }

    @Test
    void validateItems_shouldThrowException_whenQuantityIsZeroOrNegative() {
        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item")
                .quantity(0L)
                .unitPrice(100.0);

        assertThrows(InvalidInvoiceItemException.class, () -> validateItem(invoiceItemRequest));
    }

    @Test
    void validateItems_shouldThrowException_whenQuantityTooLarge() {
        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item")
                .quantity(10_000_000_000L)
                .unitPrice(100.0);

        assertThrows(InvalidInvoiceItemException.class, () -> validateItem(invoiceItemRequest));
    }

    @Test
    void validateItems_shouldThrowException_whenUnitPriceIsNegative() {
        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item")
                .quantity(1L)
                .unitPrice(-1.0);

        assertThrows(InvalidInvoiceItemException.class, () -> validateItem(invoiceItemRequest));
    }

    @Test
    void validateItems_shouldThrowException_whenUnitPriceIsTooLarge() {
        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item")
                .quantity(1L)
                .unitPrice(100_000_000.0);

        assertThrows(InvalidInvoiceItemException.class, () -> validateItem(invoiceItemRequest));
    }

    @Test
    void validateItems_shouldThrowException_whenUnitPriceHasTooManyDecimals() {
        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item")
                .quantity(1L)
                .unitPrice(10.123);

        assertThrows(InvalidInvoiceItemException.class, () -> validateItem(invoiceItemRequest));
    }

    @Test
    void validateDates_shouldPass_whenIssueDateBeforeOrEqualDueDate() {
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = LocalDate.now().plusDays(1);

        assertDoesNotThrow(() -> InvoiceValidator.validateDates(issueDate, dueDate));
        assertDoesNotThrow(() -> InvoiceValidator.validateDates(issueDate, issueDate));
    }

    @Test
    void validateDates_shouldThrowException_whenIssueDateAfterDueDate() {
        LocalDate issueDate = LocalDate.now().plusDays(1);
        LocalDate dueDate = LocalDate.now();

        assertThrows(InvalidInvoiceDateException.class, () -> InvoiceValidator.validateDates(issueDate, dueDate));
    }

    private void validateItem(InvoiceItemRequest item) {
        InvoiceValidator.validateItems(List.of(item));
    }
}
