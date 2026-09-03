package com.hospital.hms.service;

import com.hospital.hms.dto.DoctorDto;
import com.hospital.hms.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DoctorService {

    Doctor create(DoctorDto dto);

    Doctor update(Long id, DoctorDto dto);

    void delete(Long id);

    Doctor getById(Long id);

    List<Doctor> getAll();

    List<Doctor> getAvailable();

    Page<Doctor> getPage(Pageable pageable);

    Page<Doctor> search(String keyword, Pageable pageable);

    long countAll();
}
