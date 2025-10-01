package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.InvoiceItem;
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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
    void createInvoice_shouldUseExistingClient_whenClientExists() {
        String clientNip = "12345678";
        InvoiceRequest invoiceRequest = buildValidInvoiceRequest(clientNip);

        Client mockClient = new Client();

        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.of(mockClient));
        when(invoiceRepository.existsByInvoiceNumber(anyString())).thenReturn(false);

        Invoice existingInvoice = Invoice.builder().id(1L).build();

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(existingInvoice);

        Long invoiceId = invoiceService.createInvoice(invoiceRequest);

        assertNotNull(invoiceId);
        assertEquals(1L, invoiceId);

        verify(clientRepository).findByNip(clientNip);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void createInvoice_shouldCreateNewClient_whenClientDoesNotExist() {
        String clientNip = "12345678";
        InvoiceRequest invoiceRequest = buildValidInvoiceRequest(clientNip);

        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.empty());
        when(invoiceRepository.existsByInvoiceNumber(anyString())).thenReturn(false);

        Client mockClient = new Client();

        Invoice existingInvoice  = Invoice.builder()
                .id(1L)
                .build();

        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(existingInvoice);

        Long invoiceId = invoiceService.createInvoice(invoiceRequest);

        assertNotNull(invoiceId);
        assertEquals(1L, invoiceId);

        verify(clientRepository).findByNip(clientNip);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void createInvoice_shouldThrowException_whenInvoiceNumberExists() {
        String invoiceNumber = "FV/2025/01/01/00001";
        String clientNip = "12345678";

        InvoiceRequest invoiceRequest = buildValidInvoiceRequest(clientNip);

        Client mockClient = new Client();

        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.of(mockClient));
        when(invoiceRepository.existsByInvoiceNumber(invoiceNumber)).thenReturn(true);

        assertThrows(InvoiceNumberExistsException.class, () -> invoiceService.createInvoice(invoiceRequest));
    }

    @Test
    void createInvoice_shouldReturnSavedInvoiceId() {
        String invoiceNumber = "FV/2025/01/01/00001";
        String clientNip = "12345678";

        InvoiceRequest invoiceRequest = buildValidInvoiceRequest(clientNip);

        Client mockClient = new Client();

        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.of(mockClient));
        when(invoiceRepository.existsByInvoiceNumber(invoiceNumber)).thenReturn(false);

        Invoice existingInvoice  = Invoice.builder()
                .id(1L)
                .build();

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(existingInvoice);

        Long invoiceId = invoiceService.createInvoice(invoiceRequest);

        assertNotNull(invoiceId);
        assertEquals(1L, invoiceId);

        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_shouldGenerateInvoiceNumber_whenInvoiceNumberIsBlank() {
        String clientNip = "12345678";

        InvoiceRequest invoiceRequest = buildValidInvoiceRequest(clientNip);
        invoiceRequest.setInvoiceNumber("");

        Client mockClient = new Client();
        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.of(mockClient));

        Invoice savedInvoice = Invoice.builder().id(1L).build();
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        Long invoiceId = invoiceService.createInvoice(invoiceRequest);

        assertNotNull(invoiceId);
        assertEquals(1L, invoiceId);

        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void searchInvoices_shouldReturnPaginatedResponse_whenSearchIsNull() {
        Pageable pageable = PageRequest.of(0, 2);
        Invoice invoice1 = Invoice.builder().id(1L).build();
        Invoice invoice2 = Invoice.builder().id(2L).build();

        List<Invoice> invoices = List.of(invoice1, invoice2);
        Page<Invoice> invoicePage = new PageImpl<>(invoices, pageable, invoices.size());

        when(invoiceRepository.findAll(pageable)).thenReturn(invoicePage);

        PaginatedInvoiceResponse mockResponse = new PaginatedInvoiceResponse();
        mockResponse.totalElements(2L);
        when(invoiceMapper.toResponse(invoicePage)).thenReturn(mockResponse);

        PaginatedInvoiceResponse response = invoiceService.searchInvoices(0, 2, null);

        assertNotNull(response);
        assertEquals(2L, response.getTotalElements());
    }

    @Test
    void searchInvoices_shouldReturnPaginatedResponse_whenSearchIsEmpty() {
        Pageable pageable = PageRequest.of(0, 2);
        Invoice invoice1 = Invoice.builder().id(1L).build();
        Invoice invoice2 = Invoice.builder().id(2L).build();

        List<Invoice> invoices = List.of(invoice1, invoice2);
        Page<Invoice> invoicePage = new PageImpl<>(invoices, pageable, invoices.size());

        when(invoiceRepository.findAll(pageable)).thenReturn(invoicePage);

        PaginatedInvoiceResponse mockResponse = new PaginatedInvoiceResponse();
        mockResponse.totalElements(2L);
        when(invoiceMapper.toResponse(invoicePage)).thenReturn(mockResponse);

        PaginatedInvoiceResponse response = invoiceService.searchInvoices(0, 2, "");

        assertNotNull(response);
        assertEquals(2L, response.getTotalElements());
    }

    @Test
    void searchInvoices_showReturnSearchedInvoices_whenInvoiceNumberIsProvided() {
        Pageable pageable = PageRequest.of(0, 2);
        Invoice invoice1 = Invoice.builder().id(1L).invoiceNumber("FV/2025/01/01/00001").build();
        Invoice invoice2 = Invoice.builder().id(2L).invoiceNumber("FV/2025/01/01/00002").build();

        List<Invoice> invoices = List.of(invoice1, invoice2);
        Page<Invoice> invoicePage = new PageImpl<>(invoices, pageable, invoices.size());

        when(invoiceRepository.findAll(ArgumentMatchers.<Specification<Invoice>>any(), eq(pageable))).thenReturn(invoicePage);

        PaginatedInvoiceResponse mockResponse = new PaginatedInvoiceResponse();
        mockResponse.totalElements(1L);
        when(invoiceMapper.toResponse(invoicePage)).thenReturn(mockResponse);

        PaginatedInvoiceResponse response = invoiceService.searchInvoices(0, 2, "FV/2025/01/01/00001");

        assertNotNull(response);
        assertEquals(1L, response.getTotalElements());
    }

    @Test
    void searchInvoices_showReturnEmptyResponse_whenNoInvoiceFound() {
        Pageable pageable = PageRequest.of(0, 2);
        Invoice invoice1 = Invoice.builder().id(1L).invoiceNumber("FV/2025/01/01/00001").build();
        Invoice invoice2 = Invoice.builder().id(2L).invoiceNumber("FV/2025/01/01/00002").build();

        List<Invoice> invoices = List.of(invoice1, invoice2);
        Page<Invoice> invoicePage = new PageImpl<>(invoices, pageable, invoices.size());

        when(invoiceRepository.findAll(ArgumentMatchers.<Specification<Invoice>>any(), eq(pageable))).thenReturn(invoicePage);

        PaginatedInvoiceResponse mockResponse = new PaginatedInvoiceResponse();
        mockResponse.totalElements(0L);
        when(invoiceMapper.toResponse(invoicePage)).thenReturn(mockResponse);

        PaginatedInvoiceResponse response = invoiceService.searchInvoices(0, 2, "FV/2025/01/01/00003");

        assertNotNull(response);
        assertEquals(0L, response.getTotalElements());
    }

    @Test
    void deleteInvoice_shouldDeleteInvoice_whenInvoiceExists() {
        Long invoiceId = 1L;

        when(invoiceRepository.existsById(invoiceId)).thenReturn(true);

        invoiceService.deleteInvoiceById(invoiceId);

        verify(invoiceRepository, times(1)).deleteById(invoiceId);
    }

    @Test
    void deleteInvoice_shouldThrowException_whenInvoiceDoesNotExist() {
        Long invoiceId = 1L;

        when(invoiceRepository.existsById(invoiceId)).thenReturn(false);

        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.deleteInvoiceById(invoiceId));
        verify(invoiceRepository, never()).deleteById(invoiceId);
    }

    @Test
    void updateInvoice_shouldThrowException_whenInvoiceDoesNotExist() {
        Long invoiceId = 1L;

        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdateRequest();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.updateInvoiceById(invoiceId, invoiceUpdateRequest));
    }

    @Test
    void updateInvoice_shouldThrowException_whenInvoiceNumberExistsForAnotherInvoice() {
        Long invoiceId = 1L;
        String clientNip = "123456789";

        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdateRequest();

        InvoiceItem existingItem = buildValidInvoiceItem();

        Invoice existingInvoice  = Invoice.builder()
                .id(1L)
                .items(new ArrayList<>(List.of(existingItem)))
                .build();

        Client mockClient = new Client();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(existingInvoice));
        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.of(mockClient));
        when(invoiceRepository.existsByInvoiceNumberAndIdNot(invoiceUpdateRequest.getInvoiceNumber(), invoiceId)).thenReturn(true);

        assertThrows(InvoiceNumberExistsException.class, () -> invoiceService.updateInvoiceById(invoiceId, invoiceUpdateRequest));
    }

    @Test
    void updateInvoice_shouldReturnUpdatedInvoiceId() {
        Long invoiceId = 1L;
        String clientNip = "123456789";

        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdateRequest();

        InvoiceItem existingItem = buildValidInvoiceItem();

        Invoice existingInvoice  = Invoice.builder()
                .id(1L)
                .items(new ArrayList<>(List.of(existingItem)))
                .build();

        Client mockClient = new Client();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(existingInvoice));
        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.of(mockClient));
        when(invoiceRepository.existsByInvoiceNumberAndIdNot(invoiceUpdateRequest.getInvoiceNumber(), invoiceId)).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(existingInvoice);

        Long updatedInvoiceId = invoiceService.updateInvoiceById(invoiceId, invoiceUpdateRequest);

        assertNotNull(updatedInvoiceId);
        assertEquals(1L, updatedInvoiceId);

        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void updateInvoice_shouldUseExistingClient_whenClientExists() {
        Long invoiceId = 1L;
        String clientNip = "123456789";

        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdateRequest();

        InvoiceItem existingItem = buildValidInvoiceItem();

        Invoice existingInvoice  = Invoice.builder()
                .id(1L)
                .items(new ArrayList<>(List.of(existingItem)))
                .build();

        Client mockClient = new Client();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(existingInvoice));
        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.of(mockClient));
        when(invoiceRepository.existsByInvoiceNumberAndIdNot(invoiceUpdateRequest.getInvoiceNumber(), invoiceId)).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(existingInvoice);

        Long updatedInvoiceId = invoiceService.updateInvoiceById(invoiceId, invoiceUpdateRequest);

        assertNotNull(updatedInvoiceId);
        assertEquals(1L, updatedInvoiceId);

        verify(clientRepository).findByNip(clientNip);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateInvoice_shouldCreateNewClient_whenClientDoesNotExists() {
        Long invoiceId = 1L;
        String clientNip = "123456789";

        InvoiceUpdateRequest invoiceUpdateRequest = buildValidInvoiceUpdateRequest();

        InvoiceItem existingItem = buildValidInvoiceItem();

        Invoice existingInvoice  = new Invoice();
        existingInvoice.setId(1L);
        existingInvoice.setItems(new ArrayList<>(List.of(existingItem)));

        Client mockClient = new Client();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(existingInvoice));
        when(clientRepository.findByNip(clientNip)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);
        when(invoiceRepository.existsByInvoiceNumberAndIdNot(invoiceUpdateRequest.getInvoiceNumber(), invoiceId)).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(existingInvoice);

        Long updatedInvoiceId = invoiceService.updateInvoiceById(invoiceId, invoiceUpdateRequest);

        assertNotNull(updatedInvoiceId);
        assertEquals(1L, updatedInvoiceId);

        verify(clientRepository).findByNip(clientNip);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void getInvoiceByPublicToken_shouldThrowException_whenInvoiceDoesNotExist() {
        String publicToken = "d201d0dc-006f-4faa-8fc3-7783ba1b5c8e";

        when(invoiceRepository.findByPublicToken(publicToken)).thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.getInvoiceByPublicToken(publicToken));
    }

    @Test
    void getInvoiceByPublicToken_shouldReturnInvoiceResponse_whenInvoiceExists() {
        String token = "d201d0dc-006f-4faa-8fc3-7783ba1b5c8e";
        Invoice mockInvoice = new Invoice();

        when(invoiceRepository.findByPublicToken(token)).thenReturn(Optional.of(mockInvoice));
        when(invoiceMapper.toResponse(mockInvoice)).thenReturn(new InvoiceResponse().id(1L));

        InvoiceResponse invoiceResponse = invoiceService.getInvoiceByPublicToken(token);

        assertNotNull(invoiceResponse);
        assertEquals(1L, invoiceResponse.getId());
    }

    private InvoiceRequest buildValidInvoiceRequest(String clientNip) {
        InvoiceItemRequest invoiceItemRequest = new InvoiceItemRequest()
                .description("Item 1")
                .quantity(1L)
                .unitPrice(100.0);

        ClientRequest clientRequest = new ClientRequest()
                .name("Client1")
                .nip(clientNip);

        return new InvoiceRequest()
                .invoiceNumber("FV/2025/01/01/00001")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .items(List.of(invoiceItemRequest))
                .client(clientRequest);
    }

    private InvoiceUpdateRequest buildValidInvoiceUpdateRequest() {
        InvoiceItemUpdateRequest invoiceItemUpdateRequest = new InvoiceItemUpdateRequest();
        invoiceItemUpdateRequest.setId(1L);
        invoiceItemUpdateRequest.setDescription("Item 1");
        invoiceItemUpdateRequest.setQuantity(1L);
        invoiceItemUpdateRequest.setUnitPrice(100.0);

        ClientRequest clientRequest = new ClientRequest()
                .name("Client1")
                .nip("123456789");

        return new InvoiceUpdateRequest()
                .invoiceNumber("FV/2025/01/01/00001")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(7))
                .items(List.of(invoiceItemUpdateRequest))
                .client(clientRequest);
    }

    private InvoiceItem buildValidInvoiceItem() {
        return InvoiceItem.builder()
                .id(1L)
                .quantity(1L)
                .unitPrice(BigDecimal.valueOf(1))
                .build();
    }
}
