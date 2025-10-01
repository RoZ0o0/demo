package com.example.demo.entity;


import com.example.demo.models.InvoiceItemRequest;
import com.example.demo.models.InvoiceItemUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceItemTest {

    private InvoiceItem invoiceItem;

    @BeforeEach
    void setUp() {
        invoiceItem = new InvoiceItem();
    }

    @Test
    void updateFromRequest_shouldSetFieldsAndCalulateTotals() {
        InvoiceItemRequest request = new InvoiceItemRequest()
                .description("Item 1")
                .quantity(2L)
                .unitPrice(100.0);

        invoiceItem.updateFromRequest(request, null);

        assertEquals("Item 1", invoiceItem.getDescription());
        assertEquals(2L, invoiceItem.getQuantity());
        assertEquals(BigDecimal.valueOf(100.0), invoiceItem.getUnitPrice());
        assertEquals(BigDecimal.valueOf(200.0).setScale(2, RoundingMode.HALF_UP), invoiceItem.getNetValue());
        assertEquals(BigDecimal.valueOf(46.0).setScale(2, RoundingMode.HALF_UP), invoiceItem.getVatValue());
        assertEquals(BigDecimal.valueOf(246.0).setScale(2, RoundingMode.HALF_UP), invoiceItem.getGrossValue());
    }

    @Test
    void updateFromUpdateRequest_shouldSetFieldsAndCalculateTotals() {
        InvoiceItemUpdateRequest invoiceItemUpdateRequest = new InvoiceItemUpdateRequest()
                .description("Item 1")
                .quantity(2L)
                .unitPrice(100.0);

        invoiceItem.updateFromUpdateRequest(invoiceItemUpdateRequest, null);

        assertEquals("Item 1", invoiceItem.getDescription());
        assertEquals(2L, invoiceItem.getQuantity());
        assertEquals(BigDecimal.valueOf(100.0), invoiceItem.getUnitPrice());
        assertEquals(BigDecimal.valueOf(200).setScale(2, RoundingMode.HALF_UP), invoiceItem.getNetValue());
        assertEquals(BigDecimal.valueOf(46).setScale(2, RoundingMode.HALF_UP), invoiceItem.getVatValue());
        assertEquals(BigDecimal.valueOf(246).setScale(2, RoundingMode.HALF_UP), invoiceItem.getGrossValue());
    }
}
