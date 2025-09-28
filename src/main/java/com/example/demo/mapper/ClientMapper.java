package com.example.demo.mapper;

import com.example.demo.entity.Client;
import com.example.demo.models.ClientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    ClientResponse toResponse(Client client);

    List<ClientResponse> toResponse(List<Client> clients);
}
