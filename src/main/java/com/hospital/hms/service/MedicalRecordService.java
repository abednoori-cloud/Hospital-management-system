package com.hospital.hms.service;

import com.hospital.hms.dto.MedicalRecordDto;
import com.hospital.hms.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MedicalRecordService {

    MedicalRecord create(MedicalRecordDto dto);

    MedicalRecord update(Long id, MedicalRecordDto dto);

    void delete(Long id);

    MedicalRecord getById(Long id);

    List<MedicalRecord> getAll();

    List<MedicalRecord> getByPatient(Long patientId);

    Page<MedicalRecord> getPage(Pageable pageable);

    Page<MedicalRecord> search(String keyword, Pageable pageable);

    Page<MedicalRecord> getPageForDoctor(Long doctorId, Pageable pageable);

    Page<MedicalRecord> searchForDoctor(Long doctorId, String keyword, Pageable pageable);

    List<MedicalRecord> getRecentForDoctor(Long doctorId);

    long countForDoctor(Long doctorId);
}
