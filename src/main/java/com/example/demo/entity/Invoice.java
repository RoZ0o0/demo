package com.example.demo.entity;

import com.example.demo.models.InvoiceItemUpdateRequest;
import com.example.demo.models.InvoiceRequest;
import com.example.demo.models.InvoiceUpdateRequest;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Column(unique = true, nullable = false)
    private String publicToken = UUID.randomUUID().toString();

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


    public Invoice updateFromRequest(InvoiceRequest invoiceRequest, Client client) {
        this.invoiceNumber = invoiceRequest.getInvoiceNumber();
        this.issueDate = invoiceRequest.getIssueDate();
        this.dueDate = invoiceRequest.getDueDate();
        this.client = client.updateFromRequest(invoiceRequest.getClient());
        this.items = invoiceRequest.getItems().stream()
                .map(itemReq -> new InvoiceItem().updateFromRequest(itemReq, this))
                .toList();
        recalculateTotals();
        return this;
    }

    public void updateFromUpdateRequest(InvoiceUpdateRequest invoiceUpdateRequest, Client client) {
        this.issueDate = invoiceUpdateRequest.getIssueDate();
        this.dueDate = invoiceUpdateRequest.getDueDate();
        this.client = client.updateFromRequest(invoiceUpdateRequest.getClient());
        updateInvoiceItems(invoiceUpdateRequest.getItems());
        recalculateTotals();
    }

    private void updateInvoiceItems(List<InvoiceItemUpdateRequest> itemRequests) {
        Map<Long, InvoiceItem> existingItemsMap = this.items.stream()
                .filter(i -> i.getId() != null)
                .collect(Collectors.toMap(InvoiceItem::getId, Function.identity()));

        Set<Long> updatedIds = new HashSet<>();
        List<InvoiceItem> newItems = new ArrayList<>();

        for (InvoiceItemUpdateRequest itemReq : itemRequests) {
            if (itemReq.getId() != null) {
                InvoiceItem existingItem = existingItemsMap.get(itemReq.getId());
                if (existingItem == null) {
                    throw new IllegalArgumentException("Item with ID " + itemReq.getId() + " not found in invoice.");
                }
                existingItem.updateFromUpdateRequest(itemReq, this);
                updatedIds.add(itemReq.getId());
            } else {
                InvoiceItem newItem = new InvoiceItem().updateFromUpdateRequest(itemReq, this);
                newItems.add(newItem);
            }
        }

        this.items.removeIf(item -> item.getId() != null && !updatedIds.contains(item.getId()));

        this.items.addAll(newItems);
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
