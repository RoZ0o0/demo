package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.mapper.ClientMapper;
import com.example.demo.models.CheckClientNipExistsResponse;
import com.example.demo.models.PaginatedClientResponse;
import com.example.demo.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
    void getClientsPaginated_shouldReturnPaginatedResponses() {
        Pageable pageable = PageRequest.of(0, 2);
        Client client1 = Client.builder().id(1L).build();
        Client client2 = Client.builder().id(2L).build();

        List<Client> clientList = List.of(client1, client2);
        Page<Client> clientPage = new PageImpl<>(clientList, pageable, clientList.size());

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);

        PaginatedClientResponse mockResponse = new PaginatedClientResponse();
        mockResponse.totalElements(2L);
        when(clientMapper.toResponse(clientPage)).thenReturn(mockResponse);

        PaginatedClientResponse response = clientService.getClientsPaginated(0, 2);

        assertNotNull(response);
        assertEquals(2L, response.getTotalElements());
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
}
