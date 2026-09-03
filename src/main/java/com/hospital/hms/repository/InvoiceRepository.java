package com.hospital.hms.repository;

import com.hospital.hms.entity.Invoice;
import com.hospital.hms.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByPatientIdOrderByInvoiceDateDesc(Long patientId);

    List<Invoice> findByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT COALESCE(SUM(i.treatmentCost), 0) FROM Invoice i WHERE i.paymentStatus = 'PAID'")
    java.math.BigDecimal sumRevenue();

    long countByInvoiceDate(java.time.LocalDate invoiceDate);

    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate = :date ORDER BY i.createdAt DESC")
    List<Invoice> findByInvoiceDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT i FROM Invoice i WHERE " +
            "LOWER(i.patient.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.patient.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.treatmentDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Invoice> search(@Param("keyword") String keyword, Pageable pageable);
}
