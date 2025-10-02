package com.example.demo.specification;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.entity.Client;
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
import org.springframework.data.jpa.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ClientSpecificationsTest {

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

        clientRepository.saveAll(List.of(client1, client2));
    }

    @AfterEach
    void tearDown() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @ParameterizedTest(name = "search = {0}, expectedName = {1}")
    @CsvSource({
            "'Client 1', 'Client 1'",
            "'123456789', 'Client 1'",
            "'client2@example.com', 'Client 2'",
            "'Street 2', 'Client 2'"
    })
    void searchClient_shouldFindByMultipleFields(String search, String expectedName) {
        Specification<Client> spec = ClientSpecifications.searchClient(search);
        List<Client> results = clientRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo(expectedName);
    }

    @ParameterizedTest
    @CsvSource({
            "Street, 2",
            "client3, 0"
    })
    void searchClient_shouldReturnExpectedNumberOfMatches(String search, int expectedCount) {
        Specification<Client> spec = ClientSpecifications.searchClient(search);
        List<Client> results = clientRepository.findAll(spec);

        assertThat(results).hasSize(expectedCount);
    }
}
