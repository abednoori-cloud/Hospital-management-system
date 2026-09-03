package com.hospital.hms.repository;

import com.hospital.hms.entity.Appointment;
import com.hospital.hms.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Appointment> findByStatusOrderByAppointmentDateTimeAsc(AppointmentStatus status);

    List<Appointment> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT a FROM Appointment a WHERE " +
            "LOWER(a.patient.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.patient.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.doctor.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.doctor.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.doctor.department) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Appointment> search(@Param("keyword") String keyword, Pageable pageable);

    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND (" +
            "LOWER(a.patient.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.patient.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.reason) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Appointment> searchByDoctorId(@Param("doctorId") Long doctorId, @Param("keyword") String keyword, Pageable pageable);

    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateTimeAsc(Long doctorId, AppointmentStatus status);

    long countByDoctorIdAndAppointmentDateTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentDateTime BETWEEN :start AND :end AND a.status <> 'CANCELLED'")
    List<Appointment> findConflicts(@Param("doctorId") Long doctorId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);
}
