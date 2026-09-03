package com.hospital.hms.service.impl;

import com.hospital.hms.dto.AppointmentDto;
import com.hospital.hms.entity.Appointment;
import com.hospital.hms.entity.AppointmentStatus;
import com.hospital.hms.entity.Doctor;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.AppointmentRepository;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.service.AppointmentService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
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

    private void checkConflict(Long doctorId, LocalDateTime dateTime, Long ignoreAppointmentId) {
        LocalDateTime start = dateTime.minusMinutes(29);
        LocalDateTime end = dateTime.plusMinutes(29);
        boolean conflict = appointmentRepository.findConflicts(doctorId, start, end).stream()
                .anyMatch(a -> ignoreAppointmentId == null || !a.getId().equals(ignoreAppointmentId));
        if (conflict) {
            throw new EntityExistsException("This doctor already has an appointment around this time slot.");
        }
    }

    @Override
    public Appointment book(AppointmentDto dto) {
        Patient patient = findPatient(dto.getPatientId());
        Doctor doctor = findDoctor(dto.getDoctorId());
        checkConflict(doctor.getId(), dto.getAppointmentDateTime(), null);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDateTime(dto.getAppointmentDateTime())
                .reason(dto.getReason())
                .status(dto.getStatus() != null ? dto.getStatus() : AppointmentStatus.SCHEDULED)
                .notes(dto.getNotes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Booked appointment with id {}", saved.getId());
        return saved;
    }

    @Override
    public Appointment update(Long id, AppointmentDto dto) {
        Appointment appointment = getById(id);
        Patient patient = findPatient(dto.getPatientId());
        Doctor doctor = findDoctor(dto.getDoctorId());
        checkConflict(doctor.getId(), dto.getAppointmentDateTime(), id);

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(dto.getAppointmentDateTime());
        appointment.setReason(dto.getReason());
        appointment.setStatus(dto.getStatus() != null ? dto.getStatus() : appointment.getStatus());
        appointment.setNotes(dto.getNotes());

        log.info("Updated appointment with id {}", id);
        return appointmentRepository.save(appointment);
    }

    @Override
    public void cancel(Long id) {
        Appointment appointment = getById(id);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        log.info("Cancelled appointment with id {}", id);
    }

    @Override
    public void complete(Long id) {
        Appointment appointment = getById(id);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        log.info("Marked appointment {} as completed", id);
    }

    @Override
    public void delete(Long id) {
        Appointment appointment = getById(id);
        appointmentRepository.delete(appointment);
        log.info("Deleted appointment with id {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment getById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAll() {
        return appointmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Appointment> getPage(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Appointment> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getPage(pageable);
        }
        return appointmentRepository.search(keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Appointment> getPageForDoctor(Long doctorId, Pageable pageable) {
        return appointmentRepository.findByDoctorId(doctorId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Appointment> searchForDoctor(Long doctorId, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getPageForDoctor(doctorId, pageable);
        }
        return appointmentRepository.searchByDoctorId(doctorId, keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatusOrderByAppointmentDateTimeAsc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getRecent() {
        return appointmentRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return appointmentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);
        return appointmentRepository.countByAppointmentDateTimeBetween(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTodayForDoctor(Long doctorId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);
        return appointmentRepository.countByDoctorIdAndAppointmentDateTimeBetween(doctorId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCompletedForDoctor(Long doctorId) {
        return appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED);
    }
}
