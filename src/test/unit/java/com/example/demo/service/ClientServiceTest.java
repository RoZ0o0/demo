package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.exception.ClientNotFoundException;
import com.example.demo.exception.NipAlreadyExistsException;
import com.example.demo.mapper.ClientMapper;
import com.example.demo.models.CheckClientNipExistsResponse;
import com.example.demo.models.ClientRequest;
import com.example.demo.models.PaginatedClientResponse;
import com.example.demo.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {
    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Test
    void searchClients_shouldReturnAllPaginatedResponses_whenSearchIsNull() {
        Pageable pageable = PageRequest.of(0, 2);
        Client client1 = Client.builder().id(1L).build();
        Client client2 = Client.builder().id(2L).build();

        List<Client> clientList = List.of(client1, client2);
        Page<Client> clientPage = new PageImpl<>(clientList, pageable, clientList.size());

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);

        PaginatedClientResponse mockResponse = new PaginatedClientResponse();
        mockResponse.totalElements(2L);
        when(clientMapper.toResponse(clientPage)).thenReturn(mockResponse);

        PaginatedClientResponse response = clientService.searchClients(0, 2, null);

        assertNotNull(response);
        assertEquals(2L, response.getTotalElements());
    }

    @Test
    void searchClients_shouldReturnAllPaginatedResponses_whenSearchIsEmpty() {
        Pageable pageable = PageRequest.of(0, 2);
        Client client1 = Client.builder().id(1L).build();
        Client client2 = Client.builder().id(2L).build();

        List<Client> clientList = List.of(client1, client2);
        Page<Client> clientPage = new PageImpl<>(clientList, pageable, clientList.size());

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);

        PaginatedClientResponse mockResponse = new PaginatedClientResponse();
        mockResponse.totalElements(2L);
        when(clientMapper.toResponse(clientPage)).thenReturn(mockResponse);

        PaginatedClientResponse response = clientService.searchClients(0, 2, "");

        assertNotNull(response);
        assertEquals(2L, response.getTotalElements());
    }

    @Test
    void searchClients_shouldReturnMaxClientLimit_whenSizeIsNull() {
        Pageable pageable = PageRequest.of(0, 10000);

        Client client1 = Client.builder().id(1L).build();
        Client client2 = Client.builder().id(2L).build();
        List<Client> clientList = List.of(client1, client2);
        Page<Client> clientPage = new PageImpl<>(clientList, pageable, clientList.size());

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);

        PaginatedClientResponse mockResponse = new PaginatedClientResponse();
        mockResponse.totalElements(2L);
        when(clientMapper.toResponse(clientPage)).thenReturn(mockResponse);

        PaginatedClientResponse response = clientService.searchClients(0, null, null);

        assertNotNull(response);
        assertEquals(2L, response.getTotalElements());
    }

    @Test
    void searchClients_shouldReturnSearchedClients_whenInvoiceNumber() {
        Pageable pageable = PageRequest.of(0, 2);

        Client client1 = Client.builder().id(1L).name("client1").build();
        Client client2 = Client.builder().id(2L).name("client2").build();
        List<Client> clientList = List.of(client1, client2);
        Page<Client> clientPage = new PageImpl<>(clientList, pageable, clientList.size());

        when(clientRepository.findAll(ArgumentMatchers.<Specification<Client>>any(), eq(pageable)))
                .thenReturn(clientPage);

        PaginatedClientResponse mockResponse = new PaginatedClientResponse();
        mockResponse.totalElements(1L);
        when(clientMapper.toResponse(clientPage)).thenReturn(mockResponse);

        PaginatedClientResponse response = clientService.searchClients(0, 2, "client1");

        assertNotNull(response);
        assertEquals(1L, response.getTotalElements());
    }

    @Test
    void searchClient_shouldReturnEmptyResponse_whenNoClientFound() {
        Pageable pageable = PageRequest.of(0, 2);

        Client client1 = Client.builder().id(1L).name("client1").build();
        Client client2 = Client.builder().id(2L).name("client2").build();
        List<Client> clientList = List.of(client1, client2);
        Page<Client> clientPage = new PageImpl<>(clientList, pageable, clientList.size());

        when(clientRepository.findAll(ArgumentMatchers.<Specification<Client>>any(), eq(pageable)))
                .thenReturn(clientPage);

        PaginatedClientResponse mockResponse = new PaginatedClientResponse();
        mockResponse.totalElements(0L);
        when(clientMapper.toResponse(clientPage)).thenReturn(mockResponse);

        PaginatedClientResponse response = clientService.searchClients(0, 2, "client3");

        assertNotNull(response);
        assertEquals(0L, response.getTotalElements());
    }

    @Test
    void checkClientNipExists_shouldReturnTrue_whenClientNipExists() {
        String nip = "12345678";

        when(clientRepository.existsByNip(nip)).thenReturn(true);

        CheckClientNipExistsResponse response = clientService.checkNipExists(nip);

        assertNotNull(response);
        assertEquals(true, response.getExists());
    }

    @Test
    void checkClientNipExists_shouldReturnFalse_whenClientNipDoesNotExist() {
        String nip = "12345678";

        when(clientRepository.existsByNip(nip)).thenReturn(false);

        CheckClientNipExistsResponse response = clientService.checkNipExists(nip);

        assertNotNull(response);
        assertEquals(false, response.getExists());
    }

    @Test
    void createClient_shouldReturnSavedClientId() {
        ClientRequest clientRequest = new ClientRequest().name("client1").nip("123456789");

        Client savedClient = Client.builder().id(1L).build();

        when(clientRepository.existsByNip(clientRequest.getNip())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        Long clientId = clientService.createClient(clientRequest);

        assertNotNull(clientId);
        assertEquals(1L, clientId);
    }

    @Test
    void createClient_shouldSaveClient_whenNipDoesNotExists() {
        ClientRequest clientRequest = new ClientRequest().name("client1").nip("123456789");

        Client savedClient = Client.builder().id(1L).build();

        when(clientRepository.existsByNip(clientRequest.getNip())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        Long clientId = clientService.createClient(clientRequest);

        assertNotNull(clientId);
        assertEquals(1L, clientId);

        verify(clientRepository).existsByNip(clientRequest.getNip());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void createClient_shouldThrowException_whenNipAlreadyExists() {
        ClientRequest clientRequest = new ClientRequest().name("client1").nip("123456789");

        when(clientRepository.existsByNip(clientRequest.getNip())).thenReturn(true);

        assertThrows(NipAlreadyExistsException.class, () -> clientService.createClient(clientRequest));
    }

    @Test
    void updateClient_shouldUpdateClient_whenNipIsUnchanged() {
        Long clientId = 1L;

        ClientRequest clientRequest = new ClientRequest().name("Client1").nip("123456789");

        Client existingClient = Client.builder().id(clientId).build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.findByNip(clientRequest.getNip())).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenReturn(existingClient);

        Long updatedClientId = clientService.updateClientById(clientId, clientRequest);

        assertEquals(clientId, updatedClientId);
        assertEquals(existingClient.getNip(), clientRequest.getNip());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void updateClient_shouldUpdateClient_whenChangedNipIsUnique() {
        Long clientId = 1L;

        ClientRequest clientRequest = new ClientRequest().name("Client1").nip("123456789");

        Client existingClient = Client.builder().id(clientId).build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.findByNip(clientRequest.getNip())).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(existingClient);

        Long updateClientId = clientService.updateClientById(clientId, clientRequest);

        assertEquals(clientId, updateClientId);
        assertEquals(existingClient.getNip(), clientRequest.getNip());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void updateClient_shouldThrowException_whenNipAlreadyExistsForAnotherClient() {
        Long clientId = 1L;

        ClientRequest clientRequest = new ClientRequest().name("Client1").nip("123456789");

        Client existingClient1 = Client.builder().id(clientId).build();
        Client existingClient2 = Client.builder().id(2L).build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient1));
        when(clientRepository.findByNip(clientRequest.getNip())).thenReturn(Optional.of(existingClient2));

        assertThrows(NipAlreadyExistsException.class, () -> clientService.updateClientById(clientId, clientRequest));
    }

    @Test
    void updateClient_shouldThrowException_whenClientDoesNotExists() {
        Long clientId = 1L;

        ClientRequest clientRequest = new ClientRequest().name("Client1").nip("123456789");

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> clientService.updateClientById(clientId, clientRequest));
    }
}
