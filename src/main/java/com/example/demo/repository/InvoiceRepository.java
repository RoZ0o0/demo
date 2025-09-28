package com.example.demo.repository;

import com.example.demo.entity.Invoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice,Long> {

    boolean existsByInvoiceNumber(String invoiceNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT MAX(CAST(SUBSTRING(i.invoiceNumber, LENGTH(:prefix) + 1) AS int)) " +
            "FROM Invoice i WHERE i.invoiceNumber LIKE :prefix%")
    Integer findMaxSuffixByPrefix(@Param("prefix") String prefix);
}
