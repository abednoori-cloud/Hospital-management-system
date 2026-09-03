package com.hospital.hms.controller;

import com.hospital.hms.entity.AppointmentStatus;
import com.hospital.hms.entity.Role;
import com.hospital.hms.security.CustomUserDetails;
import com.hospital.hms.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * A single /dashboard route that renders a completely different set of
 * widgets depending on the signed-in user's role:
 * - ADMIN: hospital-wide totals, revenue, and an appointment-status chart.
 * - DOCTOR: today's schedule, assigned patients, recent records, completed visits.
 * - RECEPTIONIST: today's bookings, waiting patients, today's invoices, recent registrations.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final InvoiceService invoiceService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        Role role = principal.getUser().getRole();
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("dashboardRole", role.name());

        switch (role) {
            case ADMIN -> loadAdminDashboard(model);
            case DOCTOR -> loadDoctorDashboard(principal, model);
            case RECEPTIONIST -> loadReceptionistDashboard(model);
        }

        return "dashboard";
    }

    private void loadAdminDashboard(Model model) {
        model.addAttribute("totalPatients", patientService.countAll());
        model.addAttribute("totalDoctors", doctorService.countAll());
        model.addAttribute("totalUsers", userService.getAll().size());
        model.addAttribute("totalAppointments", appointmentService.countAll());
        model.addAttribute("revenue", invoiceService.getTotalRevenue());
        model.addAttribute("recentAppointments", appointmentService.getRecent());

        // Simple appointment-status breakdown for the dashboard bar chart.
        Map<String, Integer> statusCounts = new java.util.LinkedHashMap<>();
        for (AppointmentStatus s : AppointmentStatus.values()) {
            statusCounts.put(s.name(), appointmentService.getByStatus(s).size());
        }
        model.addAttribute("statusCounts", statusCounts);
        int maxStatusCount = statusCounts.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        model.addAttribute("maxStatusCount", Math.max(maxStatusCount, 1));
    }

    private void loadDoctorDashboard(CustomUserDetails principal, Model model) {
        Long doctorId = principal.getDoctorId();
        if (doctorId == null) {
            model.addAttribute("unlinkedDoctorAccount", true);
            model.addAttribute("todaysAppointments", 0L);
            model.addAttribute("assignedPatients", 0L);
            model.addAttribute("completedVisits", 0L);
            model.addAttribute("recentMedicalRecords", List.of());
            return;
        }
        model.addAttribute("todaysAppointments", appointmentService.countTodayForDoctor(doctorId));
        model.addAttribute("assignedPatients", patientService.getPageForDoctor(doctorId,
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
        model.addAttribute("completedVisits", appointmentService.countCompletedForDoctor(doctorId));
        model.addAttribute("recentMedicalRecords", medicalRecordService.getRecentForDoctor(doctorId));
    }

    private void loadReceptionistDashboard(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("todaysAppointments", appointmentService.countToday());

        long waitingPatients = appointmentService.getByStatus(AppointmentStatus.SCHEDULED).stream()
                .filter(a -> a.getAppointmentDateTime().toLocalDate().isEqual(today))
                .count();
        model.addAttribute("waitingPatients", waitingPatients);

        model.addAttribute("todaysInvoices", invoiceService.countToday());
        model.addAttribute("recentRegistrations", patientService.getRecent());
    }
}
