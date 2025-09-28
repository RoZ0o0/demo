package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.mapper.ClientMapper;
import com.example.demo.models.ClientResponse;
import com.example.demo.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public List<ClientResponse> getClients() {
        List<Client> clientResponses = clientRepository.findAll();
        return clientMapper.toResponse(clientResponses);
    }
}
