package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.InvoiceItem;
import com.example.demo.exception.InvoiceNumberExistsException;
import com.example.demo.mapper.InvoiceMapper;
import com.example.demo.models.InvoiceRequest;
import com.example.demo.models.InvoiceResponse;
import com.example.demo.models.PaginatedInvoiceResponse;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.util.InvoiceCalculator;
import com.example.demo.util.InvoiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final ClientRepository clientRepository;

    public InvoiceResponse getInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository
                .findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    public Long createInvoice(InvoiceRequest invoiceRequest) {
        String invoiceNumber = invoiceRequest.getInvoiceNumber();

        if (invoiceNumber != null && !invoiceNumber.isBlank()) {
            if (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
                throw new InvoiceNumberExistsException(invoiceNumber);
            }
        } else {
            LocalDate invoiceDate = invoiceRequest.getIssueDate();
            String prefix = String.format("FV/%04d/%02d/%02d/", invoiceDate.getYear(), invoiceDate.getMonthValue(), invoiceDate.getDayOfMonth());

            Integer maxSuffix = invoiceRepository.findMaxSuffixByPrefix(prefix);
            int nextNumber = (maxSuffix == null ? 1 : maxSuffix + 1);

            invoiceNumber = prefix + String.format("%05d", nextNumber);
        }

        Client client;
        String nip = invoiceRequest.getClient().getNip();

        if (nip != null && !nip.isBlank()) {
            client = clientRepository.findByNip(nip)
                    .orElseGet(() -> clientRepository.save(new Client().updateFromRequest(invoiceRequest.getClient())));
        } else {
            client = clientRepository.save(new Client().updateFromRequest(invoiceRequest.getClient()));
        }

        invoiceRequest.getItems().forEach(InvoiceValidator::validateItem);

        List<InvoiceItem> items = invoiceRequest.getItems().stream()
                .map(InvoiceCalculator::calculateItem)
                .toList();

        BigDecimal totalNet = InvoiceCalculator.sumNet(items);
        BigDecimal totalVat = InvoiceCalculator.sumVat(items);
        BigDecimal totalGross = InvoiceCalculator.sumGross(items);

        InvoiceValidator.validateTotals(totalNet, totalVat, totalGross);

        InvoiceValidator.validateDates(invoiceRequest.getIssueDate(), invoiceRequest.getDueDate());

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .issueDate(Date.from(invoiceRequest.getIssueDate().atStartOfDay(ZoneOffset.UTC).toInstant()))
                .dueDate(Date.from(invoiceRequest.getDueDate().atStartOfDay(ZoneOffset.UTC).toInstant()))
                .client(client)
                .totalNet(totalNet)
                .totalVat(totalVat)
                .totalGross(totalGross)
                .items(items)
                .build();

        items.forEach(item -> item.setInvoice(invoice));

        return invoiceRepository.save(invoice).getId();
    }

    public PaginatedInvoiceResponse getInvoicesPaginated(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return invoiceMapper.toResponse(invoiceRepository.findAll(pageable));
    }
}
