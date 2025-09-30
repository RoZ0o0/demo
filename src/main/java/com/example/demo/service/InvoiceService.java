package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.Invoice;
import com.example.demo.exception.InvoiceNotFoundException;
import com.example.demo.exception.InvoiceNumberExistsException;
import com.example.demo.mapper.InvoiceMapper;
import com.example.demo.models.InvoiceRequest;
import com.example.demo.models.InvoiceResponse;
import com.example.demo.models.PaginatedInvoiceResponse;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.util.InvoiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
        Client client = resolveClient(invoiceRequest);

        Invoice invoice = new Invoice().updateFromRequest(invoiceRequest, client);

        String invoiceNumber = resolveInvoiceNumber(invoiceRequest);
        invoice.setInvoiceNumber(invoiceNumber);

        return invoiceRepository.save(invoice).getId();
    }

    public PaginatedInvoiceResponse getInvoicesPaginated(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return invoiceMapper.toResponse(invoiceRepository.findAll(pageable));
    }

    public InvoiceResponse getInvoiceByPublicToken(String publicToken) {
        Invoice invoice = invoiceRepository.findByPublicToken(publicToken)
                .orElseThrow(() -> new InvoiceNotFoundException(publicToken));
        return invoiceMapper.toResponse(invoice);
    }

    private String resolveInvoiceNumber (InvoiceRequest invoiceRequest) {
        String invoiceNumber = invoiceRequest.getInvoiceNumber();

        if (!invoiceNumber.isBlank()) {
            if (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
                throw new InvoiceNumberExistsException(invoiceNumber);
            }
            return invoiceNumber;
        }

        return generateInvoiceNumber(invoiceRequest.getIssueDate());
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

    private Client resolveClient(InvoiceRequest invoiceRequest) {
        String nip = invoiceRequest.getClient().getNip();

        return clientRepository.findByNip(nip)
                .orElseGet(() -> clientRepository.save(new Client().updateFromRequest(invoiceRequest.getClient())));
    }
}
