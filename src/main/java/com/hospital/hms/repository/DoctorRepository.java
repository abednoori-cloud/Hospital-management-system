package com.hospital.hms.repository;

import com.hospital.hms.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    List<Doctor> findByAvailableTrue();

    @Query("SELECT d FROM Doctor d WHERE " +
            "LOWER(d.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.department) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Doctor> search(@Param("keyword") String keyword, Pageable pageable);
}
