package com.example.demo.mapper;

import com.example.demo.entity.Invoice;
import com.example.demo.models.InvoiceResponse;
import com.example.demo.models.PaginatedInvoiceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.data.domain.Page;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ClientMapper.class})
public interface InvoiceMapper {

    InvoiceResponse toResponse(Invoice invoice);

    PaginatedInvoiceResponse toResponse(Page<Invoice> invoicePage);
}
