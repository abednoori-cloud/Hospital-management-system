package com.hospital.hms.service.impl;

import com.hospital.hms.dto.MedicalRecordDto;
import com.hospital.hms.entity.Doctor;
import com.hospital.hms.entity.MedicalRecord;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.MedicalRecordRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.service.MedicalRecordService;
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
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    private Patient findPatient(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }

    private Doctor findDoctor(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
    }

    @Override
    public MedicalRecord create(MedicalRecordDto dto) {
        MedicalRecord record = MedicalRecord.builder()
                .patient(findPatient(dto.getPatientId()))
                .doctor(findDoctor(dto.getDoctorId()))
                .visitDate(dto.getVisitDate())
                .diagnosis(dto.getDiagnosis())
                .prescription(dto.getPrescription())
                .doctorNotes(dto.getDoctorNotes())
                .build();
        MedicalRecord saved = medicalRecordRepository.save(record);
        log.info("Created medical record with id {}", saved.getId());
        return saved;
    }

    @Override
    public MedicalRecord update(Long id, MedicalRecordDto dto) {
        MedicalRecord record = getById(id);
        record.setPatient(findPatient(dto.getPatientId()));
        record.setDoctor(findDoctor(dto.getDoctorId()));
        record.setVisitDate(dto.getVisitDate());
        record.setDiagnosis(dto.getDiagnosis());
        record.setPrescription(dto.getPrescription());
        record.setDoctorNotes(dto.getDoctorNotes());
        log.info("Updated medical record with id {}", id);
        return medicalRecordRepository.save(record);
    }

    @Override
    public void delete(Long id) {
        MedicalRecord record = getById(id);
        medicalRecordRepository.delete(record);
        log.info("Deleted medical record with id {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecord getById(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecord> getAll() {
        return medicalRecordRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecord> getByPatient(Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecord> getPage(Pageable pageable) {
        return medicalRecordRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecord> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getPage(pageable);
        }
        return medicalRecordRepository.search(keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecord> getPageForDoctor(Long doctorId, Pageable pageable) {
        return medicalRecordRepository.findByDoctorId(doctorId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecord> searchForDoctor(Long doctorId, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getPageForDoctor(doctorId, pageable);
        }
        return medicalRecordRepository.searchByDoctorId(doctorId, keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecord> getRecentForDoctor(Long doctorId) {
        return medicalRecordRepository.findTop5ByDoctorIdOrderByVisitDateDesc(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countForDoctor(Long doctorId) {
        return medicalRecordRepository.countByDoctorId(doctorId);
    }
}
