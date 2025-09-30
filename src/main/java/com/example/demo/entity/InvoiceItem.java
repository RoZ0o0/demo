package com.example.demo.entity;

import com.example.demo.models.InvoiceItemRequest;
import com.example.demo.models.InvoiceItemUpdateRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255)
    @NotNull
    private String description;

    @NotNull
    @Digits(integer = 12, fraction = 2)
    private Long quantity;

    @NotNull
    @Digits(integer = 12, fraction = 2)
    private BigDecimal unitPrice;

    @NotNull
    @Digits(integer = 5, fraction = 2)
    private BigDecimal vatRate;

    @NotNull
    @Digits(integer = 12, fraction = 2)
    private BigDecimal netValue;

    @NotNull
    @Digits(integer = 12, fraction = 2)
    private BigDecimal vatValue;

    @NotNull
    @Digits(integer = 12, fraction = 2)
    private BigDecimal grossValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    public InvoiceItem updateFromRequest(InvoiceItemRequest request, Invoice invoice) {
        this.invoice = invoice;
        this.description = request.getDescription();
        this.quantity = request.getQuantity();
        this.unitPrice = BigDecimal.valueOf(request.getUnitPrice());
        this.vatRate = BigDecimal.valueOf(request.getVatRate());

        recalculateTotals();
        return this;
    }

    public InvoiceItem updateFromUpdateRequest(InvoiceItemUpdateRequest request, Invoice invoice) {
        this.invoice = invoice;
        this.description = request.getDescription();
        this.quantity = request.getQuantity();
        this.unitPrice = BigDecimal.valueOf(request.getUnitPrice());
        this.vatRate = BigDecimal.valueOf(request.getVatRate());

        recalculateTotals();
        return this;
    }

    private void recalculateTotals() {
        this.netValue = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        this.vatValue = netValue.multiply(vatRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        this.grossValue = netValue.add(vatValue).setScale(2, RoundingMode.HALF_UP);
    }
}
