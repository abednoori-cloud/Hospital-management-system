package com.hospital.hms.controller;

import com.hospital.hms.entity.AppointmentStatus;
import com.hospital.hms.entity.PaymentStatus;
import com.hospital.hms.service.AppointmentService;
import com.hospital.hms.service.DoctorService;
import com.hospital.hms.service.InvoiceService;
import com.hospital.hms.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADMIN-only reporting / analytics dashboard: hospital-wide totals,
 * appointment-status breakdown, and invoice counts by payment status.
 */
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final InvoiceService invoiceService;

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("totalPatients", patientService.countAll());
        model.addAttribute("totalDoctors", doctorService.countAll());
        model.addAttribute("totalAppointments", appointmentService.countAll());
        model.addAttribute("totalRevenue", invoiceService.getTotalRevenue());

        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        for (AppointmentStatus s : AppointmentStatus.values()) {
            statusCounts.put(s.name(), appointmentService.getByStatus(s).size());
        }
        model.addAttribute("statusCounts", statusCounts);
        int maxStatusCount = statusCounts.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        model.addAttribute("maxStatusCount", Math.max(maxStatusCount, 1));

        Map<String, Long> invoicesByStatus = new LinkedHashMap<>();
        for (PaymentStatus s : PaymentStatus.values()) {
            invoicesByStatus.put(s.name(), (long) invoiceService.getAll().stream()
                    .filter(i -> i.getPaymentStatus() == s).count());
        }
        model.addAttribute("invoicesByStatus", invoicesByStatus);

        model.addAttribute("activePage", "reports");
        return "reports";
    }
}
