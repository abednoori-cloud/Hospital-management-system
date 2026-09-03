package com.hospital.hms.dto;

import com.hospital.hms.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {

    private Long id;

    @NotNull(message = "Please select a patient")
    private Long patientId;

    @NotNull(message = "Please select a doctor")
    private Long doctorId;

    @NotNull(message = "Appointment date and time is required")
    private LocalDateTime appointmentDateTime;

    private String reason;

    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    private String notes;
}
