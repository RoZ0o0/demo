package com.example.demo.entity;

import com.example.demo.models.InvoiceRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
    private LocalDate issueDate;

    @NotNull
    private LocalDate dueDate;

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

    @NotNull
    @NotEmpty(message = "Invoice must have at least one item")
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvoiceItem> items;

    @CreationTimestamp
    private OffsetDateTime createdAt;


    public Invoice updateFromRequest(InvoiceRequest request, Client client) {
        this.issueDate = request.getIssueDate();
        this.dueDate = request.getDueDate();
        this.client = client.updateFromRequest(request.getClient());
        this.items = request.getItems().stream()
                .map(itemReq -> new InvoiceItem().updateFromRequest(itemReq, this))
                .toList();
        recalculateTotals();
        return this;
    }

    private void recalculateTotals() {
        this.totalNet = items.stream()
                .map(InvoiceItem::getNetValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalVat = items.stream()
                .map(InvoiceItem::getVatValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalGross = items.stream()
                .map(InvoiceItem::getGrossValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
