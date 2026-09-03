package com.hospital.hms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDto {

    private Long id;

    @NotNull(message = "Please select a patient")
    private Long patientId;

    @NotNull(message = "Please select a doctor")
    private Long doctorId;

    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    private String prescription;

    private String doctorNotes;
}
