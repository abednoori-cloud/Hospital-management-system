package com.hospital.hms.service.impl;

import com.hospital.hms.dto.DoctorDto;
import com.hospital.hms.entity.Doctor;
import com.hospital.hms.exception.DuplicateResourceException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.service.DoctorService;
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
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    public Doctor create(DoctorDto dto) {
        if (doctorRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Doctor", "email", dto.getEmail());
        }
        if (doctorRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new DuplicateResourceException("Doctor", "phone number", dto.getPhoneNumber());
        }
        Doctor doctor = Doctor.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .specialization(dto.getSpecialization())
                .department(dto.getDepartment())
                .yearsOfExperience(dto.getYearsOfExperience())
                .consultationFee(dto.getConsultationFee())
                .available(dto.isAvailable())
                .build();
        Doctor saved = doctorRepository.save(doctor);
        log.info("Created doctor with id {}", saved.getId());
        return saved;
    }

    @Override
    public Doctor update(Long id, DoctorDto dto) {
        Doctor doctor = getById(id);

        doctorRepository.findAll().stream()
                .filter(d -> d.getEmail().equalsIgnoreCase(dto.getEmail()) && !d.getId().equals(id))
                .findFirst()
                .ifPresent(d -> {
                    throw new DuplicateResourceException("Doctor", "email", dto.getEmail());
                });

        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setEmail(dto.getEmail());
        doctor.setPhoneNumber(dto.getPhoneNumber());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setDepartment(dto.getDepartment());
        doctor.setYearsOfExperience(dto.getYearsOfExperience());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setAvailable(dto.isAvailable());

        log.info("Updated doctor with id {}", id);
        return doctorRepository.save(doctor);
    }

    @Override
    public void delete(Long id) {
        Doctor doctor = getById(id);
        if (!doctor.getAppointments().isEmpty() || !doctor.getMedicalRecords().isEmpty()) {
            throw new com.hospital.hms.exception.OperationNotAllowedException(
                    "Cannot delete " + doctor.getFullName() + ": they still have appointments or medical " +
                            "records on file. Reassign or remove that history first, or mark the doctor as " +
                            "unavailable instead of deleting the profile.");
        }
        doctorRepository.delete(doctor);
        log.info("Deleted doctor with id {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Doctor getById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getAll() {
        return doctorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getAvailable() {
        return doctorRepository.findByAvailableTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Doctor> getPage(Pageable pageable) {
        return doctorRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Doctor> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getPage(pageable);
        }
        return doctorRepository.search(keyword.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return doctorRepository.count();
    }
}
