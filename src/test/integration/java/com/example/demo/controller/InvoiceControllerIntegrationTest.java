package com.example.demo.controller;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.entity.Client;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.InvoiceItem;
import com.example.demo.models.*;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.InvoiceItemRepository;
import com.example.demo.repository.InvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import io.restassured.specification.RequestSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class InvoiceControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    private Invoice testInvoice1;

    private Invoice testInvoice2;

    private Client testClient;

    private RequestSpecification givenURL() {
        return given().baseUri("http://localhost").port(this.port).contentType(ContentType.JSON)
                .auth().basic("admin", "secret");
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

        Invoice invoice1 = Invoice.builder()
                .invoiceNumber("FV/2025/01/01/00001")
                .publicToken("f47ac10b-58cc-4372-a567-0e02b2c3d479")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .totalNet(BigDecimal.valueOf(400))
                .totalVat(BigDecimal.valueOf(46))
                .totalGross(BigDecimal.valueOf(492))
                .client(client)
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

        Invoice invoice2 = Invoice.builder()
                .invoiceNumber("FV/2025/01/01/00002")
                .publicToken("d72ns1c2-58cc-4372-a567-13133d473321")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .totalNet(BigDecimal.valueOf(400))
                .totalVat(BigDecimal.valueOf(46))
                .totalGross(BigDecimal.valueOf(492))
                .client(client)
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

        testClient = clientRepository.save(client);

        testInvoice1 = invoiceRepository.save(invoice1);
        testInvoice2 = invoiceRepository.save(invoice2);
    }

    @AfterEach
    void tearDown() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void createInvoice_shouldCreateInvoice() throws Exception {
        InvoiceRequest invoiceRequest = buildValidInvoice();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        givenURL()
            .body(mapper.writeValueAsString(invoiceRequest))
        .when()
            .post("/invoice")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("$", is(notNullValue()));
    }

    @Test
    void createInvoice_shouldCreateInvoiceWithGeneratedInvoiceNumber_whenInvoiceNumberIsBlank() throws Exception {
        InvoiceRequest invoiceRequest = buildValidInvoice();
        invoiceRequest.setInvoiceNumber("");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        givenURL()
            .body(mapper.writeValueAsString(invoiceRequest))
        .when()
            .post("/invoice")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("$", is(notNullValue()));
    }

    @Test
    void createInvoice_shouldReturnBadRequest_whenClientNipIsMissing() throws Exception {
        InvoiceRequest invoiceRequest = buildValidInvoice();
        invoiceRequest.getClient().setNip(null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        givenURL()
            .body(mapper.writeValueAsString(invoiceRequest))
        .when()
            .post("/invoice")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createInvoice_shouldReturnBadRequest_whenIssueDateIsMissing() throws Exception {
        InvoiceRequest invoiceRequest = buildValidInvoice();
        invoiceRequest.setIssueDate(null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        givenURL()
            .body(mapper.writeValueAsString(invoiceRequest))
        .when()
            .post("/invoice")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createInvoice_shouldReturnBadRequest_whenItemsAreEmpty() throws Exception {
        InvoiceRequest invoiceRequest = buildValidInvoice();
        invoiceRequest.setItems(List.of());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        givenURL()
            .body(mapper.writeValueAsString(invoiceRequest))
        .when()
            .post("/invoice")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createInvoice_shouldReturnBadRequest_whenClientIsMissing() throws Exception {
        InvoiceRequest invoiceRequest = buildValidInvoice();
        invoiceRequest.setClient(null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        givenURL()
            .body(mapper.writeValueAsString(invoiceRequest))
        .when()
            .post("/invoice")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createInvoice_shouldReturnConflict_whenInvoiceNumberExists() throws Exception{
        InvoiceRequest invoiceRequest = buildValidInvoice();
        invoiceRequest.setInvoiceNumber(testInvoice1.getInvoiceNumber());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        givenURL()
            .body(mapper.writeValueAsString(invoiceRequest))
        .when()
            .post("/invoice")
        .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void getInvoiceById_shouldReturnInvoiceById() {
        givenURL()
            .pathParams("id", testInvoice1.getId())
        .when()
            .get("/invoice/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("invoiceNumber", equalTo(testInvoice1.getInvoiceNumber()));
    }

    @Test
    void getInvoiceById_shouldReturnBadRequest_whenInvoiceDoesNotExist() {
        givenURL()
            .pathParams("id", 10)
        .when()
            .get("/invoice/{id}")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void searchInvoices_shouldReturnEmptyInvoiceList_whenNoInvoiceExists() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();

        givenURL()
            .param("page", "0")
            .param("size", "10")
        .when()
            .get("/invoice")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("content", is(empty()))
            .body("totalElements", equalTo(0));
    }

    @Test
    void searchInvoices_shouldReturnAllInvoicesPaginated_whenSearchIsNull() {
        givenURL()
            .param("page", "0")
            .param("size", "10")
        .when()
            .get("/invoice")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("content", is(not(empty())))
            .body("totalElements", equalTo(2));
    }

    @Test
    void searchInvoices_shouldReturnAllInvoicesPaginated_whenSearchIsEmpty() {
        givenURL()
            .param("page", "0")
            .param("size", "10")
            .param("search", "")
        .when()
            .get("/invoice")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("content", is(not(empty())))
            .body("totalElements", equalTo(2));
    }

    @Test
    void searchInvoices_shouldReturnSearchedInvoicesPaginated_whenInvoiceNumberIsProvided() {
        givenURL()
            .param("page", "0")
            .param("size", "10")
            .param("search", testInvoice1.getInvoiceNumber())
        .when()
            .get("/invoice")
            .then()
        .statusCode(HttpStatus.OK.value())
            .body("content", is(not(empty())))
            .body("totalElements", equalTo(1));
    }

    @Test
    void searchInvoices_shouldReturnSearchedInvoicesPaginated_whenClientNipIsProvided() {
        givenURL()
            .param("page", "0")
            .param("size", "10")
            .param("search", testClient.getNip())
        .when()
            .get("/invoice")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("content", is(not(empty())))
            .body("totalElements", equalTo(2));
    }

    @Test
    void searchInvoices_shouldReturnBadRequest_whenPageNumberIsNotProvided() {
        givenURL()
            .param("size", "10")
            .param("search", testClient.getNip())
        .when()
            .get("/invoice")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void deleteInvoice_shouldReturnOK() {
        givenURL()
        .when()
            .delete("/invoice/{invoiceId}", testInvoice1.getId())
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    void deleteInvoice_shouldReturnBadRequest_whenInvoiceDoesNotExist() {
        givenURL()
        .when()
            .delete("/invoice/{invoiceId}", 2L)
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void getInvoiceByPublicToken_shouldReturnInvoice() {
        givenURL()
        .when()
            .get("/invoice/{publicToken}/preview", testInvoice1.getPublicToken())
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("publicToken", equalTo(testInvoice1.getPublicToken()));
    }

    @Test
    void getInvoiceByPublicToken_shouldReturnBadRequest_whenPublicTokenDoesNotExist() {
        givenURL()
        .when()
            .get("/invoice/{publicToken}/preview", "123456")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateInvoice_shouldUpdateClient() throws Exception {
        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdate();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        givenURL()
        .when()
            .body(mapper.writeValueAsString(invoiceUpdateRequest))
            .put("/invoice/{invoiceId}", testInvoice1.getId())
        .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    void updateInvoice_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdate();
        invoiceUpdateRequest.client(null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        givenURL()
        .when()
            .body(mapper.writeValueAsString(invoiceUpdateRequest))
            .put("/invoice/{invoiceId}", testInvoice1.getId())
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateInvoice_shouldReturnBadRequest_whenUpdatedInvoiceNotFound() throws Exception {
        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdate();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        givenURL()
        .when()
            .body(mapper.writeValueAsString(invoiceUpdateRequest))
            .put("/invoice/{invoiceId}", 99L)
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateInvoice_shouldReturnConflict_whenAnotherInvoiceHasSameInvoiceNumber() throws Exception {
        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdate();
        invoiceUpdateRequest.setInvoiceNumber(testInvoice2.getInvoiceNumber());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        givenURL()
        .when()
            .body(mapper.writeValueAsString(invoiceUpdateRequest))
            .put("/invoice/{invoiceId}", testInvoice1.getId())
        .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    private InvoiceRequest buildValidInvoice() {
        ClientRequest clientRequest = new ClientRequest()
                .name("Acme")
                .nip("12345678")
                .email("contact@acme.com")
                .phone("123-456-789")
                .address("Street 1");

        InvoiceItemRequest invoiceItemRequest1 = new InvoiceItemRequest()
                .description("Item 1")
                .quantity(2L)
                .unitPrice(100.0);

        InvoiceItemRequest invoiceItemRequest2 = new InvoiceItemRequest()
                .description("Item 2")
                .quantity(1L)
                .unitPrice(200.0);

        return new InvoiceRequest()
                .invoiceNumber("FV/2025/01/01/00003")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .client(clientRequest)
                .items(List.of(invoiceItemRequest1, invoiceItemRequest2));
    }

    private InvoiceUpdateRequest buildValidInvoiceUpdate() {
        ClientRequest clientRequest = new ClientRequest()
                .name("Acme")
                .nip("12345678")
                .email("contact@acme.com")
                .phone("123-456-789")
                .address("Street 1");

        InvoiceItemUpdateRequest invoiceItemUpdateRequest1 = new InvoiceItemUpdateRequest()
                .description("Item 1")
                .quantity(2L)
                .unitPrice(100.0);

        InvoiceItemUpdateRequest invoiceItemUpdateRequest2 = new InvoiceItemUpdateRequest()
                .description("Item 2")
                .quantity(1L)
                .unitPrice(200.0);

        return new InvoiceUpdateRequest()
                .invoiceNumber("FV/2025/01/01/00003")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .client(clientRequest)
                .items(List.of(invoiceItemUpdateRequest1, invoiceItemUpdateRequest2));
    }
}
