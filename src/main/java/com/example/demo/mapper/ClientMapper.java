package com.example.demo.mapper;

import com.example.demo.entity.Client;
import com.example.demo.models.ClientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    ClientResponse toResponse(Client client);
}
