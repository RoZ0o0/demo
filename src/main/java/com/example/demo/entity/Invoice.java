package com.example.demo.entity;

import com.example.demo.models.InvoiceRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50)
    @NotNull
    private String invoiceNumber;

    @NotNull
    private Date issueDate;

    @NotNull
    private Date dueDate;

    @NotNull
    @Digits(integer = 10, fraction = 2)
    private BigDecimal totalNet;

    @NotNull
    @Digits(integer = 10, fraction = 2)
    private BigDecimal totalVat;

    @NotNull
    @Digits(integer = 10, fraction = 2)
    private BigDecimal totalGross;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items;

    @CreationTimestamp
    private OffsetDateTime createdAt;


    public Invoice updateFromRequest(InvoiceRequest request, Client client) {
        this.issueDate = Date.from(request.getIssueDate().atStartOfDay(ZoneOffset.UTC).toInstant());this.dueDate = Date.from(request.getDueDate().atStartOfDay(ZoneOffset.UTC).toInstant());
        this.totalNet = BigDecimal.valueOf(request.getTotalNet());
        this.totalVat = BigDecimal.valueOf(request.getTotalVat());
        this.totalGross = BigDecimal.valueOf(request.getTotalGross());
        this.client = client.updateFromRequest(request.getClient());
        this.items = request.getItems().stream()
                .map(itemReq -> new InvoiceItem().updateFromRequest(itemReq, this))
                .toList();

        return this;
    }
}
