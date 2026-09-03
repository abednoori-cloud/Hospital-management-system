package com.hospital.hms.service;

import com.hospital.hms.dto.PatientDto;
import com.hospital.hms.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface PatientService {

    Patient create(PatientDto dto);

    Patient update(Long id, PatientDto dto);

    void delete(Long id);

    Patient getById(Long id);

    List<Patient> getAll();

    Page<Patient> getPage(Pageable pageable);

    Page<Patient> search(String keyword, Pageable pageable);

    /** Patients assigned to (i.e. with at least one appointment with) the given doctor. */
    Page<Patient> getPageForDoctor(Long doctorId, Pageable pageable);

    Page<Patient> searchForDoctor(Long doctorId, String keyword, Pageable pageable);

    long countAll();

    long countNewSince(LocalDateTime since);

    /** Most recently registered patients, for the RECEPTIONIST "Recent Registrations" dashboard widget. */
    List<Patient> getRecent();
}
