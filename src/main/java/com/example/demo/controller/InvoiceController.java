package com.example.demo.controller;

import com.example.demo.api.InvoiceApi;
import com.example.demo.models.InvoiceRequest;
import com.example.demo.models.InvoiceResponse;
import com.example.demo.models.InvoiceUpdateRequest;
import com.example.demo.models.PaginatedInvoiceResponse;
import com.example.demo.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InvoiceController implements InvoiceApi {
    private final InvoiceService invoiceService;

    @Override
    public ResponseEntity<Long> createInvoice(InvoiceRequest invoiceRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createInvoice(invoiceRequest));
    }

    @Override
    public ResponseEntity<InvoiceResponse> getInvoice(Long invoiceId) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(invoiceId));
    }

    @Override
    public ResponseEntity<Void> deleteInvoice(Long invoiceId) {
        invoiceService.deleteInvoiceById(invoiceId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Long> updateInvoice(Long invoiceId, InvoiceUpdateRequest invoiceUpdateRequest) {
        return ResponseEntity.ok(invoiceService.updateInvoiceById(invoiceId, invoiceUpdateRequest));
    }

    @Override
    public ResponseEntity<PaginatedInvoiceResponse> searchInvoices(Integer page, Integer size, String search) {
        return ResponseEntity.ok(invoiceService.searchInvoices(page, size, search));
    }

    @Override
    public ResponseEntity<InvoiceResponse> getInvoiceByPublicToken(String publicToken) {
        return ResponseEntity.ok(invoiceService.getInvoiceByPublicToken(publicToken));
    }
}
