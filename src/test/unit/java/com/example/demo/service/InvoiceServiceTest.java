package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.Invoice;
import com.example.demo.exception.InvalidInvoiceDateException;
import com.example.demo.exception.InvoiceNotFoundException;
import com.example.demo.exception.InvoiceNumberExistsException;
import com.example.demo.mapper.InvoiceMapper;
import com.example.demo.models.*;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    @InjectMocks
    private InvoiceService invoiceService;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private ClientRepository clientRepository;

    @Test
    void getInvoiceById_shouldReturnInvoice_whenInvoiceExists() {
        Long invoiceId = 1L;
        Invoice invoice = Invoice.builder().id(invoiceId).build();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toResponse(invoice)).thenReturn(new InvoiceResponse().id(invoiceId));

        InvoiceResponse invoiceResponse = invoiceService.getInvoiceById(invoiceId);

        assertNotNull(invoiceResponse);
        assertEquals(invoiceId, invoiceResponse.getId());
    }

    @Test
    void getInvoiceById_shouldThrowException_whenInvoiceDoesNotExist() {
        Long invoiceId = 1L;

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.getInvoiceById(invoiceId));
    }

    @Test
    void createInvoice_shouldThrowException_whenIssueDateIsAfterDueDate() {
        InvoiceRequest invoiceRequest = new InvoiceRequest()
                .issueDate(LocalDate.now().plusDays(1))
                .dueDate(LocalDate.now());

        assertThrows(InvalidInvoiceDateException.class, () -> invoiceService.createInvoice(invoiceRequest));
    }

    @Test
    void createInvoice_clientExists_usesExisitngClient() {
        String clientNip = "12345678";
        InvoiceRequest invoiceRequest = buildValidInvoiceRequest(clientNip);

        Client existingClient = new Client();

        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.of(existingClient));
        when(invoiceRepository.existsByInvoiceNumber(anyString())).thenReturn(false);

        Invoice savedInvoice = Invoice.builder().id(1L).build();

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        invoiceService.createInvoice(invoiceRequest);

        verify(clientRepository).findByNip(clientNip);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void createInvoice_clientDoesNotExist_createNewClient() {
        String clientNip = "12345678";
        InvoiceRequest invoiceRequest = buildValidInvoiceRequest(clientNip);

        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.empty());
        when(invoiceRepository.existsByInvoiceNumber(anyString())).thenReturn(false);

        Client savedClient = new Client();
        savedClient.setId(10L);
        savedClient.setName("Test");
        savedClient.setNip(clientNip);
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        when(invoiceRepository.save(any())).thenReturn(Invoice.builder().id(1L).build());

        invoiceService.createInvoice(invoiceRequest);

        verify(clientRepository).findByNip(clientNip);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void createInvoice_shouldThrowException_whenInvoiceNumberExists() {
        String invoiceNumber = "FV/2025/01/01/00001";
        String clientNip = "12345678";

        InvoiceRequest invoiceRequest = buildValidInvoiceRequest(clientNip);

        Client existingClient = new Client();

        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.of(existingClient));
        when(invoiceRepository.existsByInvoiceNumber(invoiceNumber)).thenReturn(true);

        assertThrows(InvoiceNumberExistsException.class, () -> invoiceService.createInvoice(invoiceRequest));
    }

    @Test
    void createInvoice_shouldReturnSavedInvoiceId() {
        String invoiceNumber = "FV/2025/01/01/00001";
        String clientNip = "12345678";

        InvoiceRequest invoiceRequest = buildValidInvoiceRequest(clientNip);

        Client existingClient = new Client();
        existingClient.setId(10L);
        existingClient.setName("Test");
        existingClient.setNip(clientNip);
        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.of(existingClient));

        when(invoiceRepository.existsByInvoiceNumber(invoiceNumber)).thenReturn(false);

        Invoice savedInvoice = Invoice.builder().id(1L).build();
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        Long invoiceId = invoiceService.createInvoice(invoiceRequest);

        assertEquals(1L, invoiceId);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void getInvoicesPaginated_shouldReturnPaginatedResponse() {
        Pageable pageable = PageRequest.of(0, 2);
        Invoice invoice1 = Invoice.builder().id(1L).build();
        Invoice invoice2 = Invoice.builder().id(2L).build();

        List<Invoice> invoices = List.of(invoice1, invoice2);
        Page<Invoice> invoicePage = new PageImpl<>(invoices, pageable, invoices.size());

        when(invoiceRepository.findAll(pageable)).thenReturn(invoicePage);

        PaginatedInvoiceResponse mockResponse = new PaginatedInvoiceResponse();
        mockResponse.totalElements(2L);
        when(invoiceMapper.toResponse(invoicePage)).thenReturn(mockResponse);

        PaginatedInvoiceResponse response = invoiceService.getInvoicesPaginated(0, 2);

        assertNotNull(response);
        assertEquals(2L, response.getTotalElements());
    }

    private InvoiceRequest buildValidInvoiceRequest(String clientNip) {
        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item 1")
                .quantity(1L)
                .unitPrice(100.0);

        ClientRequest clientRequest = new ClientRequest()
                .name("Test")
                .nip(clientNip);

        return new InvoiceRequest()
                .invoiceNumber("FV/2025/01/01/00001")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .items(List.of(invoiceItemRequest))
                .client(clientRequest);
    }
}
