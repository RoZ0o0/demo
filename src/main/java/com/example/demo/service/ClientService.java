package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.exception.ClientNotFoundException;
import com.example.demo.exception.NipAlreadyExistsException;
import com.example.demo.mapper.ClientMapper;
import com.example.demo.models.*;
import com.example.demo.repository.ClientRepository;
import com.example.demo.specification.ClientSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    private static final int MAX_CLIENT_LIMIT = 10000;

    public PaginatedClientResponse searchClients(Integer page, Integer size, String search) {
        Pageable pageable;
        pageable = size == null ? PageRequest.of(page, MAX_CLIENT_LIMIT) : PageRequest.of(page, size);

        if (search == null || search.trim().isEmpty()) {
            Page<Client> clientPage = clientRepository.findAll(pageable);
            return clientMapper.toResponse(clientPage);
        }

        Page<Client> clientPage = clientRepository.findAll(
                ClientSpecifications.searchClient(search), pageable
        );

        return clientMapper.toResponse(clientPage);
    }

    public CheckClientNipExistsResponse checkNipExists(String nip) {
        boolean exists = clientRepository.existsByNip(nip);
        CheckClientNipExistsResponse response = new CheckClientNipExistsResponse();
        response.setExists(exists);
        return response;
    }

    public Long createClient(ClientRequest clientRequest) {
        String nip = clientRequest.getNip();

        if (clientRepository.existsByNip(nip)) {
            throw new NipAlreadyExistsException(nip);
        }

        Client client = new Client().updateFromRequest(clientRequest);
        return clientRepository.save(client).getId();
    }

    public Long updateClientById(Long clientId, ClientRequest clientRequest) {
        Optional<Client> client = clientRepository.findById(clientId);

        if (client.isEmpty()) {
            throw new ClientNotFoundException(clientId);
        }

        client.get().updateFromRequest(clientRequest);

        return clientRepository.save(client.get()).getId();
    }
}
