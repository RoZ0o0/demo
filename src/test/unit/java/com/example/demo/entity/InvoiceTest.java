package com.example.demo.entity;

import com.example.demo.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InvoiceTest {

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
    }

    @Test
    void updateFromRequest_shouldUpdateInvoiceFieldsAndItems() {
        InvoiceRequest invoiceRequest = buildValidInvoiceRequest();

        Client client = new Client().updateFromRequest(invoiceRequest.getClient());

        invoice.updateFromRequest(invoiceRequest, client);

        assertEquals("FV/2025/01/01/00001", invoice.getInvoiceNumber());
        assertEquals(LocalDate.of(2025, 10, 1), invoice.getIssueDate());
        assertEquals(LocalDate.of(2025, 10, 2), invoice.getDueDate());
        assertEquals(1, invoice.getItems().size());
        assertEquals(BigDecimal.valueOf(200).setScale(2, RoundingMode.HALF_UP), invoice.getTotalNet());
        assertEquals(BigDecimal.valueOf(46).setScale(2, RoundingMode.HALF_UP), invoice.getTotalVat());
        assertEquals(BigDecimal.valueOf(246).setScale(2, RoundingMode.HALF_UP), invoice.getTotalGross());
    }

    @Test
    void updateFromUpdateRequest_shouldUpdateExistingItems() {
        invoice.setItems(new ArrayList<>());

        InvoiceItem item = InvoiceItem.builder()
                .id(1L)
                .netValue(BigDecimal.valueOf(100))
                .vatValue(BigDecimal.valueOf(23))
                .grossValue(BigDecimal.valueOf(123))
                .build();
        invoice.setItems(new ArrayList<>(List.of(item)));

        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdateRequest();

        Client client = new Client().updateFromRequest(invoiceUpdateRequest.getClient());

        invoice.updateFromUpdateRequest(invoiceUpdateRequest, client);

        assertEquals(LocalDate.of(2025, 10, 2), invoice.getIssueDate());
        assertEquals(LocalDate.of(2025, 10, 3), invoice.getDueDate());
        assertEquals(1, invoice.getItems().size());
    }

    @Test
    void updateFromUpdateRequest_shouldAddNewItem() {
        invoice.setItems(new ArrayList<>());
        InvoiceItemUpdateRequest newItem = new InvoiceItemUpdateRequest()
                .description("Item 2")
                .quantity(1L)
                .unitPrice(50.0);

        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdateRequest();

        invoiceUpdateRequest.setItems(List.of(newItem));

        Client client = new Client().updateFromRequest(invoiceUpdateRequest.getClient());

        invoice.updateFromUpdateRequest(invoiceUpdateRequest, client);

        assertEquals(1, invoice.getItems().size());
        assertEquals(BigDecimal.valueOf(50.0).setScale(2, RoundingMode.HALF_UP), invoice.getTotalNet());
    }

    @Test
    void updateFromUpdateRequest_shouldRemoveMissingItem() {
        InvoiceItem existing = InvoiceItem.builder()
                .id(1L)
                .description("Item 1")
                .quantity(1L)
                .unitPrice(BigDecimal.valueOf(100))
                .netValue(BigDecimal.valueOf(100))
                .vatValue(BigDecimal.valueOf(23))
                .grossValue(BigDecimal.valueOf(123))
                .build();

        invoice.setItems(new ArrayList<>(List.of(existing)));

        InvoiceItemUpdateRequest newItem = new InvoiceItemUpdateRequest()
                .description("Item 2")
                .quantity(1L)
                .unitPrice(50.0);

        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdateRequest();

        invoiceUpdateRequest.setItems(List.of(newItem));

        Client client = new Client().updateFromRequest(invoiceUpdateRequest.getClient());

        invoice.updateFromUpdateRequest(invoiceUpdateRequest, client);

        assertEquals(1, invoice.getItems().size());
        assertNull(invoice.getItems().getFirst().getId());
        assertEquals(BigDecimal.valueOf(50.0).setScale(2, RoundingMode.HALF_UP), invoice.getTotalNet());
    }

    @Test
    void updateFromUpdateRequest_shouldThrowException_whenIdIsInvalid() {
        InvoiceItem existing = InvoiceItem.builder()
                .id(1L)
                .build();
        invoice.setItems(new ArrayList<>(List.of(existing)));

        InvoiceItemUpdateRequest invalidItem = new InvoiceItemUpdateRequest();
        invalidItem.id(99L);

        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdateRequest();
        invoiceUpdateRequest.setItems(List.of(invalidItem));

        Client client = new Client().updateFromRequest(invoiceUpdateRequest.getClient());

        assertThrows(IllegalArgumentException.class, () -> invoice.updateFromUpdateRequest(invoiceUpdateRequest, client));
    }

    private InvoiceRequest buildValidInvoiceRequest() {
        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item 1")
                .quantity(2L)
                .unitPrice(100.0);

        ClientRequest clientRequest = new ClientRequest()
                .name("Client A")
                .nip("1234567890");

        return new InvoiceRequest()
                .invoiceNumber("FV/2025/01/01/00001")
                .issueDate(LocalDate.of(2025, 10, 1))
                .dueDate(LocalDate.of(2025, 10, 2))
                .items(List.of(invoiceItemRequest))
                .client(clientRequest);
    }

    private InvoiceUpdateRequest buildValidInvoiceUpdateRequest() {
        ClientRequest clientRequest = new ClientRequest()
                .name("Client A")
                .nip("1234567890");

        InvoiceItemUpdateRequest invoiceItemUpdateRequest = new InvoiceItemUpdateRequest()
                .id(1L)
                .description("Item1")
                .quantity(1L)
                .unitPrice(100.0);

        return new InvoiceUpdateRequest()
                .issueDate(LocalDate.of(2025, 10, 2))
                .dueDate(LocalDate.of(2025, 10, 3))
                .items(List.of(invoiceItemUpdateRequest))
                .client(clientRequest);
    }
}
