package com.example.demo.util;

import com.example.demo.entity.InvoiceItem;
import com.example.demo.models.InvoiceItemRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class InvoiceCalculator {
    public static InvoiceItem calculateItem(InvoiceItemRequest req) {
        Long quantity = req.getQuantity();
        BigDecimal unitPrice = BigDecimal.valueOf(req.getUnitPrice());
        BigDecimal vatRate = BigDecimal.valueOf(23);
        BigDecimal netValue = BigDecimal.valueOf(quantity).multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal vatValue = netValue.multiply(vatRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal grossValue = netValue.add(vatValue).setScale(2, RoundingMode.HALF_UP);



        InvoiceItem item = new InvoiceItem();
        item.setDescription(req.getDescription());
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setVatRate(vatRate);
        item.setNetValue(netValue);
        item.setVatValue(vatValue);
        item.setGrossValue(grossValue);
        return item;
    }

    public static BigDecimal sumNet(List<InvoiceItem> items) {
        return items.stream()
                .map(InvoiceItem::getNetValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal sumVat(List<InvoiceItem> items) {
        return items.stream()
                .map(InvoiceItem::getVatValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal sumGross(List<InvoiceItem> items) {
        return items.stream()
                .map(InvoiceItem::getGrossValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
