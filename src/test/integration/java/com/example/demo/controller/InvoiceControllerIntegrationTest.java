package com.example.demo.controller;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.entity.Client;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.InvoiceItem;
import com.example.demo.models.ClientRequest;
import com.example.demo.models.InvoiceItemRequest;
import com.example.demo.models.InvoiceRequest;
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
import static org.junit.jupiter.api.Assertions.*;

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

    private Invoice testInvoice;

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

        Invoice invoice = Invoice.builder()
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
                .invoice(invoice)
                .build();

        InvoiceItem invoiceItem2 = InvoiceItem.builder()
                .description("Item 2")
                .quantity(1L)
                .unitPrice(BigDecimal.valueOf(100.0))
                .vatRate(BigDecimal.valueOf(23))
                .netValue(BigDecimal.valueOf(200))
                .vatValue(BigDecimal.valueOf(46))
                .grossValue(BigDecimal.valueOf(246))
                .invoice(invoice)
                .build();

        invoice.setItems(List.of(invoiceItem1, invoiceItem2));

        clientRepository.save(client);

        testInvoice = invoiceRepository.save(invoice);
    }

    @AfterEach
    void tearDown() {
        invoiceItemRepository.deleteAll();
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void shouldCreateInvoice() throws Exception {
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

        InvoiceRequest invoiceRequest = new InvoiceRequest()
                .invoiceNumber("FV/2025/01/01/00002")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .client(clientRequest)
                .items(List.of(invoiceItemRequest1, invoiceItemRequest2));

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
    void shouldCreateInvoiceWithGeneratedInvoiceNumberWhenInvoiceNumberIsBlank() throws Exception {
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

        InvoiceRequest invoiceRequest = new InvoiceRequest()
                .invoiceNumber("")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .client(clientRequest)
                .items(List.of(invoiceItemRequest1, invoiceItemRequest2));

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Long invoiceId = givenURL()
            .body(mapper.writeValueAsString(invoiceRequest))
        .when()
            .post("/invoice")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .as(Long.class);

        Invoice savedInvoice = invoiceRepository.findById(invoiceId).orElseThrow();
        assertEquals("FV/2025/09/30/00001", savedInvoice.getInvoiceNumber());
    }

    @Test
    void shouldReturnBadRequestWhenClientNipIsMissing() throws Exception {
        ClientRequest clientRequest = new ClientRequest()
                .name("Acme")
                .nip(null)
                .email("contact@acme.com")
                .phone("123-456-789")
                .address("Street 1");

        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item 1")
                .quantity(2L)
                .unitPrice(100.0);

        InvoiceRequest invoiceRequest = new InvoiceRequest()
                .invoiceNumber("FV/2025/01/01/00003")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .client(clientRequest)
                .items(List.of(invoiceItemRequest));

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
    void shouldReturnBadRequestWhenIssueDateIsMissing() throws Exception {
        ClientRequest clientRequest = new ClientRequest()
                .name("Acme")
                .nip("8213162178")
                .email("contact@acme.com")
                .phone("123-456-789")
                .address("Street 1");

        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item 1")
                .quantity(2L)
                .unitPrice(100.0);

        InvoiceRequest invoiceRequest = new InvoiceRequest()
                .invoiceNumber("FV/2025/01/01/00003")
                .issueDate(null)
                .dueDate(LocalDate.now().plusDays(1))
                .client(clientRequest)
                .items(List.of(invoiceItemRequest));

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
    void shouldReturnBadRequestWhenItemsAreEmpty() throws Exception {
        ClientRequest clientRequest = new ClientRequest()
                .name("Acme")
                .nip("8213162178")
                .email("contact@acme.com")
                .phone("123-456-789")
                .address("Street 1");

        InvoiceRequest invoiceRequest = new InvoiceRequest()
                .invoiceNumber("FV/2025/01/01/00003")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .client(clientRequest)
                .items(List.of());

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
    void shouldReturnBadRequestWhenClientIsMissing() throws Exception {
        InvoiceRequest invoiceRequest = new InvoiceRequest()
                .invoiceNumber("FV/2025/01/01/00003")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .client(null)
                .items(List.of());

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
    void shouldReturnConflictWhenInvoiceNumberExists() throws Exception{
        ClientRequest clientRequest = new ClientRequest()
                .name("Acme")
                .nip("8213162178")
                .email("contact@acme.com")
                .phone("123-456-789")
                .address("Street 1");

        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item 1")
                .quantity(2L)
                .unitPrice(100.0);

        InvoiceRequest invoiceRequest = new InvoiceRequest()
                .invoiceNumber(testInvoice.getInvoiceNumber())
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(1))
                .client(clientRequest)
                .items(List.of(invoiceItemRequest));

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
    void shouldReturnInvoiceById() {
        givenURL()
            .pathParams("id", testInvoice.getId())
        .when()
            .get("/invoice/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("invoiceNumber", equalTo(testInvoice.getInvoiceNumber()));
    }

    @Test
    void shouldReturnBadRequestIfInvoiceDoesNotExist() {
        givenURL()
            .pathParams("id", 10)
        .when()
            .get("/invoice/{id}")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
