package com.example.demo.mapper;

import com.example.demo.entity.Client;
import com.example.demo.models.ClientResponse;
import com.example.demo.models.PaginatedClientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.data.domain.Page;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    ClientResponse toResponse(Client client);

    PaginatedClientResponse toResponse(Page<Client> clients);
}
