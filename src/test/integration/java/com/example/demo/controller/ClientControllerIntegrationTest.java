package com.example.demo.controller;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.entity.Client;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.InvoiceItemRepository;
import com.example.demo.repository.InvoiceRepository;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ClientControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private  InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    private Client testClient;

    private RequestSpecification givenURL() {
        return given().baseUri("http://localhost").port(this.port).contentType(ContentType.JSON);
    }

    @BeforeEach
    void setUp() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();

        Client client = Client.builder()
                .name("Corp")
                .nip("9876243210")
                .email("new@example.com")
                .phone("555-6789")
                .address("New Street")
                .build();

        testClient = clientRepository.save(client);
    }

    @AfterEach
    void tearDown() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void shouldReturnEmptyClientListWhenNoClientsExist() {
        clientRepository.deleteAll();

        givenURL()
        .when()
            .get("/client")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(0));
    }

    @Test
    void shouldReturnAllClients () {
        clientRepository.save(Client.builder()
                .name("Corp A")
                .nip("12345678")
                .email("a@example.com")
                .phone("111-111")
                .address("Street A")
                .build());

        givenURL()
        .when()
            .get("/client")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(2));
    }

    @Test
    void shouldReturnTrueWhenClientNipExists() {
        givenURL()
            .param("nip", testClient.getNip())
        .when()
            .get("/client/exists")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("exists", equalTo(true));
    }

    @Test
    void shouldReturnFalseWhenClientNipDoesNotExist() {
        givenURL()
            .param("nip", "999999999")
        .when()
            .get("/client/exists")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("exists", equalTo(false));
    }
}
