package com.hospital.hms.config;

import com.hospital.hms.entity.*;
import com.hospital.hms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Seeds the database with a default admin account, sample doctors, patients,
 * appointments, medical records and invoices so the application is usable
 * immediately after start-up (useful for demos, portfolios, and grading).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final InvoiceRepository invoiceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Doctor doctorForLinking = seedClinicalData();
        seedUsers(doctorForLinking);
    }

    private void seedUsers(Doctor doctorForLinking) {
        if (userRepository.count() > 0) {
            return;
        }
        log.info("Seeding default users...");

        userRepository.save(User.builder()
                .username("admin")
                .email("admin@hms.local")
                .password(passwordEncoder.encode("Admin@123"))
                .fullName("System Administrator")
                .role(Role.ADMIN)
                .enabled(true)
                .build());

        userRepository.save(User.builder()
                .username("doctor")
                .email("doctor@hms.local")
                .password(passwordEncoder.encode("Doctor@123"))
                .fullName("Dr. Sarah Mitchell")
                .role(Role.DOCTOR)
                .enabled(true)
                .doctorProfile(doctorForLinking)
                .build());

        userRepository.save(User.builder()
                .username("reception")
                .email("reception@hms.local")
                .password(passwordEncoder.encode("Reception@123"))
                .fullName("Front Desk Receptionist")
                .role(Role.RECEPTIONIST)
                .enabled(true)
                .build());
    }

    /**
     * Seeds sample clinical data if not already present, and returns the
     * "Dr. Sarah Mitchell" Doctor profile (fetching the existing one if data
     * was already seeded on a previous run) so it can be linked to the
     * default "doctor" login account.
     */
    private Doctor seedClinicalData() {
        if (patientRepository.count() > 0) {
            return doctorRepository.findAll().stream()
                    .filter(d -> "sarah.mitchell@hms.local".equals(d.getEmail()))
                    .findFirst()
                    .orElse(null);
        }
        log.info("Seeding sample clinical data...");

        Doctor d1 = doctorRepository.save(Doctor.builder()
                .firstName("Sarah").lastName("Mitchell")
                .email("sarah.mitchell@hms.local").phoneNumber("+1-202-555-0101")
                .specialization("Cardiology").department("Cardiology")
                .yearsOfExperience(12).consultationFee(new BigDecimal("150.00"))
                .available(true).build());

        Doctor d2 = doctorRepository.save(Doctor.builder()
                .firstName("James").lastName("Carter")
                .email("james.carter@hms.local").phoneNumber("+1-202-555-0102")
                .specialization("Orthopedics").department("Orthopedics")
                .yearsOfExperience(9).consultationFee(new BigDecimal("120.00"))
                .available(true).build());

        Doctor d3 = doctorRepository.save(Doctor.builder()
                .firstName("Emily").lastName("Nguyen")
                .email("emily.nguyen@hms.local").phoneNumber("+1-202-555-0103")
                .specialization("Pediatrics").department("Pediatrics")
                .yearsOfExperience(6).consultationFee(new BigDecimal("100.00"))
                .available(true).build());

        Doctor d4 = doctorRepository.save(Doctor.builder()
                .firstName("Michael").lastName("Okafor")
                .email("michael.okafor@hms.local").phoneNumber("+1-202-555-0104")
                .specialization("Neurology").department("Neurology")
                .yearsOfExperience(15).consultationFee(new BigDecimal("180.00"))
                .available(false).build());

        Patient p1 = patientRepository.save(Patient.builder()
                .firstName("John").lastName("Smith")
                .email("john.smith@example.com").phoneNumber("+1-303-555-0111")
                .dateOfBirth(LocalDate.of(1985, 4, 12)).gender(Gender.MALE)
                .address("123 Maple Street, Denver, CO").bloodGroup("O+")
                .emergencyContact("+1-303-555-0199").build());

        Patient p2 = patientRepository.save(Patient.builder()
                .firstName("Emma").lastName("Johnson")
                .email("emma.johnson@example.com").phoneNumber("+1-303-555-0112")
                .dateOfBirth(LocalDate.of(1992, 8, 25)).gender(Gender.FEMALE)
                .address("456 Oak Avenue, Denver, CO").bloodGroup("A-")
                .emergencyContact("+1-303-555-0198").build());

        Patient p3 = patientRepository.save(Patient.builder()
                .firstName("Liam").lastName("Davis")
                .email("liam.davis@example.com").phoneNumber("+1-303-555-0113")
                .dateOfBirth(LocalDate.of(2015, 1, 30)).gender(Gender.MALE)
                .address("789 Pine Road, Denver, CO").bloodGroup("B+")
                .emergencyContact("+1-303-555-0197").build());

        Patient p4 = patientRepository.save(Patient.builder()
                .firstName("Olivia").lastName("Martinez")
                .email("olivia.martinez@example.com").phoneNumber("+1-303-555-0114")
                .dateOfBirth(LocalDate.of(1978, 11, 3)).gender(Gender.FEMALE)
                .address("321 Birch Lane, Denver, CO").bloodGroup("AB+")
                .emergencyContact("+1-303-555-0196").build());

        appointmentRepository.save(Appointment.builder()
                .patient(p1).doctor(d1)
                .appointmentDateTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .reason("Routine heart checkup").status(AppointmentStatus.SCHEDULED).build());

        appointmentRepository.save(Appointment.builder()
                .patient(p2).doctor(d3)
                .appointmentDateTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(30))
                .reason("Annual physical exam").status(AppointmentStatus.SCHEDULED).build());

        appointmentRepository.save(Appointment.builder()
                .patient(p3).doctor(d3)
                .appointmentDateTime(LocalDateTime.now().minusDays(3).withHour(9).withMinute(0))
                .reason("Fever and cough").status(AppointmentStatus.COMPLETED).build());

        appointmentRepository.save(Appointment.builder()
                .patient(p4).doctor(d2)
                .appointmentDateTime(LocalDateTime.now().withHour(16).withMinute(0))
                .reason("Knee pain follow-up").status(AppointmentStatus.SCHEDULED).build());

        appointmentRepository.save(Appointment.builder()
                .patient(p1).doctor(d1)
                .appointmentDateTime(LocalDateTime.now().minusDays(10))
                .reason("ECG review").status(AppointmentStatus.CANCELLED).build());

        medicalRecordRepository.save(MedicalRecord.builder()
                .patient(p3).doctor(d3).visitDate(LocalDate.now().minusDays(3))
                .diagnosis("Acute viral pharyngitis")
                .prescription("Paracetamol 250mg 3x/day for 5 days, plenty of fluids")
                .doctorNotes("Advise rest, follow up if fever persists beyond 3 days.").build());

        medicalRecordRepository.save(MedicalRecord.builder()
                .patient(p1).doctor(d1).visitDate(LocalDate.now().minusMonths(2))
                .diagnosis("Mild hypertension")
                .prescription("Amlodipine 5mg once daily")
                .doctorNotes("Recommend low-sodium diet and regular exercise.").build());

        invoiceRepository.save(Invoice.builder()
                .patient(p3).doctor(d3).invoiceDate(LocalDate.now().minusDays(3))
                .treatmentDescription("Consultation + Medication").treatmentCost(new BigDecimal("85.00"))
                .paymentStatus(PaymentStatus.PAID).build());

        invoiceRepository.save(Invoice.builder()
                .patient(p1).doctor(d1).invoiceDate(LocalDate.now().minusMonths(2))
                .treatmentDescription("Cardiology Consultation + ECG").treatmentCost(new BigDecimal("220.00"))
                .paymentStatus(PaymentStatus.PARTIALLY_PAID).build());

        invoiceRepository.save(Invoice.builder()
                .patient(p4).doctor(d2).invoiceDate(LocalDate.now())
                .treatmentDescription("Orthopedic Consultation").treatmentCost(new BigDecimal("120.00"))
                .paymentStatus(PaymentStatus.PENDING).build());

        log.info("Sample data seeded successfully.");
        return d1;
    }
}
