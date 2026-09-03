package com.hospital.hms.dto;

import com.hospital.hms.entity.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {

    private Long id;

    @NotNull(message = "Please select a patient")
    private Long patientId;

    private Long doctorId;

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;

    @NotBlank(message = "Treatment description is required")
    private String treatmentDescription;

    @NotNull(message = "Treatment cost is required")
    @DecimalMin(value = "0.0", message = "Treatment cost cannot be negative")
    private BigDecimal treatmentCost;

    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
}
