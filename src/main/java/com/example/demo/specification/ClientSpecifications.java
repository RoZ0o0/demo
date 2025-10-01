package com.example.demo.specification;

import com.example.demo.entity.Client;
import org.springframework.data.jpa.domain.Specification;

public class ClientSpecifications {
    private ClientSpecifications() {}

    public static Specification<Client> searchClient(String search) {
        return (r, q, cb) -> {
            String like = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(r.get("name")), like),
                    cb.like(cb.lower(r.get("nip")), like),
                    cb.like(cb.lower(r.get("address")), like),
                    cb.like(cb.lower(r.get("email")), like),
                    cb.like(cb.lower(r.get("phone")), like)
            );
        };
    }
}
