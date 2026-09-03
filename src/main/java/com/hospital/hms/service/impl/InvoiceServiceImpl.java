package com.hospital.hms.service.impl;

import com.hospital.hms.dto.InvoiceDto;
import com.hospital.hms.entity.Doctor;
import com.hospital.hms.entity.Invoice;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.InvoiceRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    private Patient findPatient(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }

    @Override
    public Invoice create(InvoiceDto dto) {
        Doctor doctor = dto.getDoctorId() != null
                ? doctorRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", dto.getDoctorId()))
                : null;

        Invoice invoice = Invoice.builder()
                .patient(findPatient(dto.getPatientId()))
                .doctor(doctor)
                .invoiceDate(dto.getInvoiceDate())
                .treatmentDescription(dto.getTreatmentDescription())
                .treatmentCost(dto.getTreatmentCost())
                .paymentStatus(dto.getPaymentStatus())
                .build();
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created invoice with id {}", saved.getId());
        return saved;
    }

    @Override
    public Invoice update(Long id, InvoiceDto dto) {
        Invoice invoice = getById(id);
        Doctor doctor = dto.getDoctorId() != null
                ? doctorRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", dto.getDoctorId()))
                : null;

        invoice.setPatient(findPatient(dto.getPatientId()));
        invoice.setDoctor(doctor);
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setTreatmentDescription(dto.getTreatmentDescription());
        invoice.setTreatmentCost(dto.getTreatmentCost());
        invoice.setPaymentStatus(dto.getPaymentStatus());

        log.info("Updated invoice with id {}", id);
        return invoiceRepository.save(invoice);
    }

    @Override
    public void delete(Long id) {
        Invoice invoice = getById(id);
        invoiceRepository.delete(invoice);
        log.info("Deleted invoice with id {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> getByPatient(Long patientId) {
        return invoiceRepository.findByPatientIdOrderByInvoiceDateDesc(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Invoice> getPage(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Invoice> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getPage(pageable);
        }
        return invoiceRepository.search(keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public java.math.BigDecimal getTotalRevenue() {
        return invoiceRepository.sumRevenue();
    }

    @Override
    @Transactional(readOnly = true)
    public long countToday() {
        return invoiceRepository.countByInvoiceDate(java.time.LocalDate.now());
    }
}
