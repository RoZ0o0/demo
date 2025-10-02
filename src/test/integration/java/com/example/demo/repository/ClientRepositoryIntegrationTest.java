package com.example.demo.repository;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.entity.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ClientRepositoryIntegrationTest {
    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Client testClient;

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

        testClient = clientRepository.save(client1);
    }

    @AfterEach
    void tearDown() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void findByNip_shouldFindClientByNip() {
        Optional<Client> clientOptional = clientRepository.findByNip(testClient.getNip());
        assertThat(clientOptional).isPresent();
    }

    @Test
    void findByNip_shouldReturnEmpty_whenNipNotFound() {
        Optional<Client> clientOptional = clientRepository.findByNip("123456789");
        assertThat(clientOptional).isEmpty();
    }

    @Test
    void existsByNip_shouldReturnTrue_whenClientExistsByNip() {
        boolean exists = clientRepository.existsByNip(testClient.getNip());
        assertThat(exists).isTrue();
    }

    @Test
    void existsByNip_shouldReturnFalse_whenClientDoesNotExistByNip() {
        boolean exists = clientRepository.existsByNip("123456789");
        assertThat(exists).isFalse();
    }
}
