package com.example.demo.entity;

import com.example.demo.models.ClientRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    @Size(max = 255)
    private String name;

    @NotNull
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
        this.name = request.getName();
        this.nip = request.getNip();
        this.email = request.getEmail();
        this.phone = request.getPhone();
        this.address = request.getAddress();
        return this;
    }
}
