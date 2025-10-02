package com.example.demo.specification;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.entity.Client;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.InvoiceItem;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.InvoiceItemRepository;
import com.example.demo.repository.InvoiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class InvoiceSpecificationsTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();

        Client client1 = Client.builder()
                .name("Client 1")
                .nip("123456789")
                .email("client1@example.com")
                .phone("111-222-333")
                .address("Street 1")
                .build();

        Client client2 = Client.builder()
                .name("Client 2")
                .nip("987654321")
                .email("client2@example.com")
                .phone("444-555-666")
                .address("Street 2")
                .build();

        client1 = clientRepository.save(client1);
        client2 = clientRepository.save(client2);

        Invoice invoice1 = Invoice.builder()
                .invoiceNumber("FV/2025/01/01/00001")
                .publicToken("e3b0c442-98fc-4f42-a9c7-8d1f5f8b0a2d")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .totalNet(BigDecimal.valueOf(100))
                .totalVat(BigDecimal.valueOf(23))
                .totalGross(BigDecimal.valueOf(123))
                .client(client1)
                .build();

        InvoiceItem item1 = InvoiceItem.builder()
                .description("Item 1")
                .quantity(2L)
                .unitPrice(BigDecimal.valueOf(50))
                .vatRate(BigDecimal.valueOf(23))
                .netValue(BigDecimal.valueOf(100))
                .vatValue(BigDecimal.valueOf(23))
                .grossValue(BigDecimal.valueOf(123))
                .invoice(invoice1)
                .build();

        invoice1.setItems(new ArrayList<>(List.of(item1)));
        invoiceRepository.save(invoice1);

        Invoice invoice2 = Invoice.builder()
                .invoiceNumber("FV/2025/01/01/00002")
                .publicToken("7f9d2e15-3c6a-4b2b-9a1e-5f3c6b7e2d1a")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .totalNet(BigDecimal.valueOf(200))
                .totalVat(BigDecimal.valueOf(46))
                .totalGross(BigDecimal.valueOf(246))
                .client(client2)
                .build();

        InvoiceItem item2 = InvoiceItem.builder()
                .description("Item 2")
                .quantity(4L)
                .unitPrice(BigDecimal.valueOf(50))
                .vatRate(BigDecimal.valueOf(23))
                .netValue(BigDecimal.valueOf(200))
                .vatValue(BigDecimal.valueOf(46))
                .grossValue(BigDecimal.valueOf(246))
                .invoice(invoice2)
                .build();

        invoice2.setItems(new ArrayList<>(List.of(item2)));
        invoiceRepository.save(invoice2);
    }

    @AfterEach
    void tearDown() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @ParameterizedTest
    @CsvSource({
            "'FV/2025/01/01/00001', 'FV/2025/01/01/00001'",
            "'FV/2025/01/01/00002', 'FV/2025/01/01/00002'",
            "'Client 1', 'FV/2025/01/01/00001'",
            "'Client 2', 'FV/2025/01/01/00002'",
            "'nonexistent', ''"
    })
    void searchInvoice_shouldReturnExpectedInvoice(String search, String expectedInvoiceNumber) {
        Specification<Invoice> spec = InvoiceSpecifications.searchInvoice(search);
        List<Invoice> result = invoiceRepository.findAll(spec);

        if (expectedInvoiceNumber.isEmpty()) {
            assertThat(result).isEmpty();
        } else {
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getInvoiceNumber()).isEqualTo(expectedInvoiceNumber);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "'FV/2025/01/01/00001', 1",
            "'FV/2025/01/01/00002', 1",
            "'Client 1', 1",
            "'Client 2', 1"
    })
    void searchInvoice_shouldReturnExpectedCount(String search, int expectedCount) {
        Specification<Invoice> spec = InvoiceSpecifications.searchInvoice(search);
        long count = invoiceRepository.count(spec);
        assertThat(count).isEqualTo(expectedCount);
    }
}
