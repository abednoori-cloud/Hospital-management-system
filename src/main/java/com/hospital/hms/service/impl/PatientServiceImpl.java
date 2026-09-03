package com.hospital.hms.service.impl;

import com.hospital.hms.dto.PatientDto;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.exception.DuplicateResourceException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    public Patient create(PatientDto dto) {
        if (patientRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Patient", "email", dto.getEmail());
        }
        if (patientRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new DuplicateResourceException("Patient", "phone number", dto.getPhoneNumber());
        }
        Patient patient = Patient.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .address(dto.getAddress())
                .bloodGroup(dto.getBloodGroup())
                .emergencyContact(dto.getEmergencyContact())
                .build();
        Patient saved = patientRepository.save(patient);
        log.info("Created patient with id {}", saved.getId());
        return saved;
    }

    @Override
    public Patient update(Long id, PatientDto dto) {
        Patient patient = getById(id);

        patientRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("Patient", "email", dto.getEmail());
            }
        });

        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setEmail(dto.getEmail());
        patient.setPhoneNumber(dto.getPhoneNumber());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setAddress(dto.getAddress());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setEmergencyContact(dto.getEmergencyContact());

        log.info("Updated patient with id {}", id);
        return patientRepository.save(patient);
    }

    @Override
    public void delete(Long id) {
        Patient patient = getById(id);
        patientRepository.delete(patient);
        log.info("Deleted patient with id {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Patient getById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> getAll() {
        return patientRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Patient> getPage(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Patient> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getPage(pageable);
        }
        return patientRepository.search(keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Patient> getPageForDoctor(Long doctorId, Pageable pageable) {
        return patientRepository.findByDoctorId(doctorId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Patient> searchForDoctor(Long doctorId, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getPageForDoctor(doctorId, pageable);
        }
        return patientRepository.searchByDoctorId(doctorId, keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return patientRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countNewSince(LocalDateTime since) {
        return patientRepository.countByCreatedAtAfter(since);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> getRecent() {
        return patientRepository.findTop5ByOrderByCreatedAtDesc();
    }
}
