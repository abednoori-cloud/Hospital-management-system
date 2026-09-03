package com.hospital.hms.service;

import com.hospital.hms.dto.InvoiceDto;
import com.hospital.hms.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InvoiceService {

    Invoice create(InvoiceDto dto);

    Invoice update(Long id, InvoiceDto dto);

    void delete(Long id);

    Invoice getById(Long id);

    List<Invoice> getAll();

    List<Invoice> getByPatient(Long patientId);

    Page<Invoice> getPage(Pageable pageable);

    Page<Invoice> search(String keyword, Pageable pageable);

    /** Sum of all PAID invoices — powers the ADMIN dashboard's Revenue widget. */
    java.math.BigDecimal getTotalRevenue();

    /** Count of invoices created today — powers the RECEPTIONIST dashboard's "Today's Invoices" widget. */
    long countToday();
}
