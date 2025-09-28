package com.example.demo.controller;

import com.example.demo.api.ClientApi;
import com.example.demo.models.ClientResponse;
import com.example.demo.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ClientController implements ClientApi {
    private final ClientService clientService;

    @Override
    public ResponseEntity<List<ClientResponse>> getClients() {
        return ResponseEntity.ok(clientService.getClients());
    }
}
