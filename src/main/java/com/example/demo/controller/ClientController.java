package com.example.demo.controller;

import com.example.demo.api.ClientApi;
import com.example.demo.models.CheckClientNipExistsResponse;
import com.example.demo.models.ClientRequest;
import com.example.demo.models.PaginatedClientResponse;
import com.example.demo.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ClientController implements ClientApi {
    private final ClientService clientService;

    @Override
    public ResponseEntity<PaginatedClientResponse> getClients(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(clientService.getClientsPaginated(page, size));
    }

    @Override
    public ResponseEntity<CheckClientNipExistsResponse> checkClientNipExists(String nip) {
        return ResponseEntity.ok(clientService.checkNipExists(nip));
    }

    @Override
    public ResponseEntity<Long> createClient(ClientRequest clientRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(clientRequest));
    }

    @Override
    public ResponseEntity<Long> updateClient(Long invoiceId, ClientRequest clientRequest) {
        return ResponseEntity.ok(clientService.updateClientById(invoiceId, clientRequest));
    }


}
