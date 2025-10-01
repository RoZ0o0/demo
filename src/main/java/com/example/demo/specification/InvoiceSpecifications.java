package com.example.demo.specification;

import com.example.demo.entity.Client;
import com.example.demo.entity.Invoice;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class InvoiceSpecifications {
    private InvoiceSpecifications() {}

    public static Specification<Invoice> searchInvoice(String search) {
        return (r, q, cb) -> {
            String like = "%" + search.toLowerCase() + "%";

            Join<Invoice, Client> clientJoin = r.join("client");

            return cb.or(
                    cb.like(cb.lower(r.get("invoiceNumber")), like),
                    cb.like(cb.lower(clientJoin.get("name")), like),
                    cb.like(cb.lower(clientJoin.get("nip")), like),
                    cb.like(cb.lower(clientJoin.get("address")), like),
                    cb.like(cb.lower(clientJoin.get("email")), like),
                    cb.like(cb.lower(clientJoin.get("phone")), like)
            );
        };
    }
}
