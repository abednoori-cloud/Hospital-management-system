package com.hospital.hms.repository;

import com.hospital.hms.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientIdOrderByVisitDateDesc(Long patientId);

    @Query("SELECT m FROM MedicalRecord m WHERE " +
            "LOWER(m.patient.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.patient.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.diagnosis) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<MedicalRecord> search(@Param("keyword") String keyword, Pageable pageable);

    Page<MedicalRecord> findByDoctorId(Long doctorId, Pageable pageable);

    @Query("SELECT m FROM MedicalRecord m WHERE m.doctor.id = :doctorId AND (" +
            "LOWER(m.patient.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.patient.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.diagnosis) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<MedicalRecord> searchByDoctorId(@Param("doctorId") Long doctorId, @Param("keyword") String keyword, Pageable pageable);

    long countByDoctorId(Long doctorId);

    List<MedicalRecord> findTop5ByDoctorIdOrderByVisitDateDesc(Long doctorId);
}
