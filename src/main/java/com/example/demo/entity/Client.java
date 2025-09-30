package com.example.demo.entity;

import com.example.demo.models.ClientRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    @NotBlank
    @Size(max = 20)
    private String nip;

    @Size(max = 255)
    private String address;

    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    public Client updateFromRequest(ClientRequest request) {
        this.name = request.getName().trim();
        this.nip = request.getNip().trim();
        this.email = normalize(request.getEmail());
        this.phone = normalize(request.getPhone());
        this.address = normalize(request.getAddress());
        return this;
    }

    private String normalize(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }
}
