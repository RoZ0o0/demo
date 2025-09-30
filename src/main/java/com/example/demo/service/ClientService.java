package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.exception.ClientNotFoundException;
import com.example.demo.exception.NipAlreadyExistsException;
import com.example.demo.mapper.ClientMapper;
import com.example.demo.models.*;
import com.example.demo.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
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

    public PaginatedClientResponse getClientsPaginated(Integer page, Integer size) {
        Pageable pageable;
        pageable = size == null ? PageRequest.of(page, MAX_CLIENT_LIMIT) : PageRequest.of(page, size);
        return clientMapper.toResponse(clientRepository.findAll(pageable));
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
