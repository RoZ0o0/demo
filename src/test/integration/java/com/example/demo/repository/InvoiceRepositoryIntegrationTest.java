package com.example.demo.repository;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.entity.Client;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.InvoiceItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class InvoiceRepositoryIntegrationTest {
    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Invoice testInvoice1;

    @BeforeEach
    void setUp() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();

        Client client1 = Client.builder()
                .name("Corp")
                .nip("9876243210")
                .email("new@example.com")
                .phone("555-6789")
                .address("New Street")
                .build();

        Invoice invoice1 = Invoice.builder()
                .invoiceNumber("FV/2025/01/01/00001")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .totalNet(BigDecimal.valueOf(400))
                .totalVat(BigDecimal.valueOf(46))
                .totalGross(BigDecimal.valueOf(492))
                .client(client1)
                .build();

        InvoiceItem invoiceItem1 = InvoiceItem.builder()
                .description("Item 1")
                .quantity(2L)
                .unitPrice(BigDecimal.valueOf(100))
                .vatRate(BigDecimal.valueOf(23))
                .netValue(BigDecimal.valueOf(200))
                .vatValue(BigDecimal.valueOf(46))
                .grossValue(BigDecimal.valueOf(246))
                .invoice(invoice1)
                .build();

        InvoiceItem invoiceItem2 = InvoiceItem.builder()
                .description("Item 2")
                .quantity(1L)
                .unitPrice(BigDecimal.valueOf(100.0))
                .vatRate(BigDecimal.valueOf(23))
                .netValue(BigDecimal.valueOf(200))
                .vatValue(BigDecimal.valueOf(46))
                .grossValue(BigDecimal.valueOf(246))
                .invoice(invoice1)
                .build();

        invoice1.setItems(List.of(invoiceItem1, invoiceItem2));

        clientRepository.save(client1);

        testInvoice1 = invoiceRepository.save(invoice1);

        Client client2 = Client.builder()
                .name("Corp S.A.")
                .nip("987123112")
                .email("newcorp@example.com")
                .phone("555-6889")
                .address("New Street 2")
                .build();

        Invoice invoice2 = Invoice.builder()
                .invoiceNumber("FV/2025/01/02/00005")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .totalNet(BigDecimal.valueOf(400))
                .totalVat(BigDecimal.valueOf(46))
                .totalGross(BigDecimal.valueOf(492))
                .client(client2)
                .build();

        InvoiceItem invoiceItem3 = InvoiceItem.builder()
                .description("Item 1")
                .quantity(2L)
                .unitPrice(BigDecimal.valueOf(100))
                .vatRate(BigDecimal.valueOf(23))
                .netValue(BigDecimal.valueOf(200))
                .vatValue(BigDecimal.valueOf(46))
                .grossValue(BigDecimal.valueOf(246))
                .invoice(invoice2)
                .build();

        InvoiceItem invoiceItem4 = InvoiceItem.builder()
                .description("Item 2")
                .quantity(1L)
                .unitPrice(BigDecimal.valueOf(100.0))
                .vatRate(BigDecimal.valueOf(23))
                .netValue(BigDecimal.valueOf(200))
                .vatValue(BigDecimal.valueOf(46))
                .grossValue(BigDecimal.valueOf(246))
                .invoice(invoice2)
                .build();

        invoice2.setItems(List.of(invoiceItem3, invoiceItem4));

        clientRepository.save(client2);

        invoiceRepository.save(invoice2);
    }

    @AfterEach
    void tearDown() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void shouldReturnTrueIfInvoiceExists() {
        boolean exists = invoiceRepository.existsByInvoiceNumber(testInvoice1.getInvoiceNumber());
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseIfInvoiceDoesNotExist() {
        boolean exists = invoiceRepository.existsByInvoiceNumber("FV/2025/01/01/000099");
        assertFalse(exists);
    }

    @Test
    void shouldReturnFalseForEmptyInvoiceNumber() {
        boolean exists = invoiceRepository.existsByInvoiceNumber("");
        assertFalse(exists);
    }

    @Test
    void shouldReturnMaxSuffixForGivenPrefix() {
        Integer max = invoiceRepository.findMaxSuffixByPrefix("FV/2025/01/01/");
        assertEquals(1, max);
    }

    @Test
    void shouldReturnNullWhenNoInvoiceMatchesPrefix() {
        Integer max = invoiceRepository.findMaxSuffixByPrefix("FV/2025/03/01");
        assertNull(max);
    }

    @Test
    void shouldIgnoreInvoicesWithDifferentPrefix() {
        Integer max = invoiceRepository.findMaxSuffixByPrefix("FV/2025/01/02/");
        assertEquals(5, max);
    }
}
