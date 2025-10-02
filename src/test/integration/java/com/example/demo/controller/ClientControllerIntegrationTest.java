package com.example.demo.controller;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.entity.Client;
import com.example.demo.models.ClientRequest;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.InvoiceItemRepository;
import com.example.demo.repository.InvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private Client testClient1;

    private Client testClient2;

    private RequestSpecification givenURL() {
        return given().baseUri("http://localhost").port(this.port).contentType(ContentType.JSON)
                .auth().basic("admin", "secret");
    }

    @BeforeEach
    void setUp() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();

        Client client1 = Client.builder()
                .name("Corp A")
                .nip("9876243210")
                .email("new@example.com")
                .phone("555-6789")
                .address("New Street 1")
                .build();

        Client client2 = Client.builder()
                .name("Corp B")
                .nip("123456789")
                .email("new2@example.com")
                .phone("535-6789")
                .address("New Street 2")
                .build();

        testClient1 = clientRepository.save(client1);
        testClient2 = clientRepository.save(client2);
    }

    @AfterEach
    void tearDown() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void searchClients_shouldReturnEmptyClientList_whenNoClientsExist() {
        clientRepository.deleteAll();

        givenURL()
            .param("page", "0")
            .param("size", "10")
        .when()
            .get("/client")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("content", is(empty()))
            .body("totalElements", equalTo(0));
    }

    @Test
    void searchClients_shouldReturnAllClientsPaginated_whenSearchIsNull() {
        givenURL()
            .param("page", "0")
            .param("size", "20")
        .when()
            .get("/client")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("content", is(not(empty())))
            .body("totalElements", equalTo(2));
    }

    @Test
    void searchClients_shouldReturnAllClientsPaginated_whenSearchIsEmpty() {
        givenURL()
            .param("page", "0")
            .param("size", "20")
            .param("search", "")
        .when()
            .get("/client")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("content", is(not(empty())))
            .body("totalElements", equalTo(2));
    }

    @Test
    void searchClients_shouldReturnSearchedClientsPaginated_whenSearchIsProvided() {
        givenURL()
            .param("page", "0")
            .param("size", "20")
            .param("search", "123456789")
        .when()
            .get("/client")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("content", is(not(empty())))
            .body("totalElements", equalTo(1));
    }

    @Test
    void searchClients_shouldReturnBadRequest_whenPageNumberIsNotProvided() {
        givenURL()
            .param("size", "20")
            .param("search", "123456789")
        .when()
            .get("/client")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void checkClientNipExists_shouldReturnTrue_whenClientNipExists() {
        givenURL()
            .param("nip", testClient1.getNip())
        .when()
            .get("/client/exists")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("exists", equalTo(true));
    }

    @Test
    void checkClientNipExists_shouldReturnFalse_whenClientNipDoesNotExist() {
        givenURL()
            .param("nip", "999999999")
        .when()
            .get("/client/exists")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("exists", equalTo(false));
    }

    @Test
    void createClient_shouldCreateClient() throws Exception {
        ClientRequest clientRequest = new ClientRequest()
                .name("Corp C")
                .nip("12344321")
                .email("corpc@email.com")
                .address("Street 3")
                .phone("333-333-333");

        givenURL()
            .body(new ObjectMapper().writeValueAsString(clientRequest))
        .when()
            .post("/client")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("$", is(notNullValue()));
    }

    @Test
    void createClient_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        ClientRequest clientRequest = new ClientRequest()
                .nip("123456789")
                .email("corpc@email.com")
                .address("Street 3")
                .phone("333-333-333");

        givenURL()
            .body(new ObjectMapper().writeValueAsString(clientRequest))
        .when()
            .post("/client")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createClient_shouldReturnConflict_whenClientNipExists() throws Exception {
        ClientRequest clientRequest = new ClientRequest()
                .name("Corp C")
                .nip("123456789")
                .email("corpc@email.com")
                .address("Street 3")
                .phone("333-333-333");

        givenURL()
            .body(new ObjectMapper().writeValueAsString(clientRequest))
        .when()
            .post("/client")
        .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void updateClient_shouldUpdateClient() throws Exception {
        ClientRequest clientRequest = new ClientRequest()
                .name("Corp C")
                .nip("1231312")
                .email("corpc@email.com")
                .address("Street 3")
                .phone("333-333-333");

        givenURL()
            .body(new ObjectMapper().writeValueAsString(clientRequest))
        .when()
            .put("/client/{clientId}", testClient1.getId())
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    void updateClient_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        ClientRequest clientRequest = new ClientRequest()
                .nip("123456789")
                .email("corpc@email.com")
                .address("Street 3")
                .phone("333-333-333");

        givenURL()
            .body(new ObjectMapper().writeValueAsString(clientRequest))
        .when()
            .put("/client/{clientId}", testClient1.getId())
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateClient_shouldReturnBadRequest_whenUpdatedClientNotFound() throws Exception {
        ClientRequest clientRequest = new ClientRequest()
                .name("Corp C")
                .nip("137126371")
                .email("corpc@email.com")
                .address("Street 3")
                .phone("333-333-333");

        givenURL()
            .body(new ObjectMapper().writeValueAsString(clientRequest))
        .when()
            .put("/client/{clientId}", 99L)
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateClient_shouldReturnConflict_whenAnotherClientHasSameNip() throws Exception {
        ClientRequest clientRequest = new ClientRequest()
                .nip(testClient2.getNip())
                .name("Corp C")
                .email("corpc@email.com")
                .address("Street 3")
                .phone("333-333-333");

        givenURL()
            .body(new ObjectMapper().writeValueAsString(clientRequest))
        .when()
            .put("/client/{clientId}", testClient1.getId())
        .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }
}
