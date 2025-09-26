package com.example.demo.mapper;

import com.example.demo.entity.Invoice;
import com.example.demo.models.InvoiceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ClientMapper.class})
public interface InvoiceMapper {

    InvoiceResponse toResponse(Invoice invoice);
}
