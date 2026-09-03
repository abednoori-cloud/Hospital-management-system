package com.hospital.hms.repository;

import com.hospital.hms.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Patient> findByEmail(String email);

    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "p.phoneNumber LIKE CONCAT('%', :keyword, '%')")
    Page<Patient> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Patients "assigned" to a doctor, defined as any patient who has at
     * least one appointment with that doctor. Used to scope the DOCTOR
     * role's "My Patients" view.
     */
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.appointments a WHERE a.doctor.id = :doctorId")
    Page<Patient> findByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Patient p JOIN p.appointments a WHERE a.doctor.id = :doctorId AND (" +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "p.phoneNumber LIKE CONCAT('%', :keyword, '%'))")
    Page<Patient> searchByDoctorId(@Param("doctorId") Long doctorId, @Param("keyword") String keyword, Pageable pageable);

    long countByCreatedAtAfter(java.time.LocalDateTime dateTime);

    List<Patient> findTop5ByOrderByCreatedAtDesc();
}
