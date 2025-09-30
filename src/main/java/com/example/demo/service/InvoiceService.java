package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.Invoice;
import com.example.demo.exception.InvoiceNotFoundException;
import com.example.demo.exception.InvoiceNumberExistsException;
import com.example.demo.mapper.InvoiceMapper;
import com.example.demo.models.*;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.util.InvoiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final ClientRepository clientRepository;

    public InvoiceResponse getInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository
                .findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    public Long createInvoice(InvoiceRequest invoiceRequest) {
        InvoiceValidator.validateDates(invoiceRequest.getIssueDate(), invoiceRequest.getDueDate());
        InvoiceValidator.validateItems(invoiceRequest.getItems());
        Client client = resolveClient(invoiceRequest.getClient());

        Invoice invoice = new Invoice().updateFromRequest(invoiceRequest, client);

        String invoiceNumber = invoice.getInvoiceNumber();

        if (!invoiceNumber.isBlank()) {
            if (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
                throw new InvoiceNumberExistsException(invoice.getInvoiceNumber());
            }
        } else {
            invoiceNumber = generateInvoiceNumber(invoice.getIssueDate());
            invoice.setInvoiceNumber(invoiceNumber);
        }

        return invoiceRepository.save(invoice).getId();
    }

    public PaginatedInvoiceResponse getInvoicesPaginated(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> invoice = invoiceRepository.findAll(pageable);
        return invoiceMapper.toResponse(invoice);
    }

    public void deleteInvoiceById(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new InvoiceNotFoundException(invoiceId);
        }

        invoiceRepository.deleteById(invoiceId);
    }

    @Transactional
    public Long updateInvoiceById(Long invoiceId, InvoiceUpdateRequest invoiceUpdateRequest) {
        Optional<Invoice> optionalInvoice = invoiceRepository.findById(invoiceId);

        if (optionalInvoice.isEmpty()) {
            throw new InvoiceNotFoundException(invoiceId);
        }

        Invoice invoice = optionalInvoice.get();

        InvoiceValidator.validateDates(invoiceUpdateRequest.getIssueDate(), invoiceUpdateRequest.getDueDate());
        InvoiceValidator.validateUpdateItems(invoiceUpdateRequest.getItems());
        Client client = resolveClient(invoiceUpdateRequest.getClient());

        invoice.updateFromUpdateRequest(invoiceUpdateRequest, client);

        String requestedNumber = invoiceUpdateRequest.getInvoiceNumber();

        if (!requestedNumber.isBlank()) {
            if (invoiceRepository.existsByInvoiceNumberAndIdNot(requestedNumber, invoiceId)) {
                throw new InvoiceNumberExistsException(requestedNumber);
            }
            invoice.setInvoiceNumber(requestedNumber);
        } else {
            invoice.setInvoiceNumber(generateInvoiceNumber(invoice.getIssueDate()));
        }

        return invoiceRepository.save(invoice).getId();
    }

    public InvoiceResponse getInvoiceByPublicToken(String publicToken) {
        Invoice invoice = invoiceRepository.findByPublicToken(publicToken)
                .orElseThrow(() -> new InvoiceNotFoundException(publicToken));
        return invoiceMapper.toResponse(invoice);
    }

    private String generateInvoiceNumber (LocalDate invoiceDate) {
        String prefix = String.format("FV/%04d/%02d/%02d/",
                invoiceDate.getYear(),
                invoiceDate.getMonthValue(),
                invoiceDate.getDayOfMonth()
        );

        Integer maxSuffix = invoiceRepository.findMaxSuffixByPrefix(prefix);
        int nextNumber = (maxSuffix == null ? 1 : maxSuffix + 1);

        return prefix + String.format("%05d", nextNumber);
    }

    private Client resolveClient(ClientRequest clientRequest) {
        String nip = clientRequest.getNip();

        return clientRepository.findByNip(nip)
                .orElseGet(() -> clientRepository.save(new Client().updateFromRequest(clientRequest)));
    }
}
