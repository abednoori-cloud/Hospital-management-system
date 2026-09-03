package com.hospital.hms.controller;

import com.hospital.hms.entity.Role;
import com.hospital.hms.security.CustomUserDetails;
import com.hospital.hms.service.AppointmentService;
import com.hospital.hms.service.DoctorService;
import com.hospital.hms.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Provides a single global search box (used from the navbar) that looks
 * across patients, doctors, and appointments at once.
 *
 * Results are scoped to match the same permission matrix as the rest of the
 * app: a DOCTOR only ever sees their own assigned patients and appointments
 * here (never the full hospital directory), and never sees the doctor
 * directory itself (they have no access to /doctors at all).
 */
@Controller
@RequiredArgsConstructor
public class GlobalSearchController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query,
                          @AuthenticationPrincipal CustomUserDetails principal,
                          Model model) {
        if (query == null) {
            query = "";
        }

        boolean isDoctor = principal.getUser().getRole() == Role.DOCTOR;
        Long doctorId = principal.getDoctorId();

        if (isDoctor) {
            model.addAttribute("patients", doctorId != null
                    ? patientService.searchForDoctor(doctorId, query, PageRequest.of(0, 10)).getContent()
                    : List.of());
            model.addAttribute("doctors", List.of());
            model.addAttribute("appointments", doctorId != null
                    ? appointmentService.searchForDoctor(doctorId, query, PageRequest.of(0, 10)).getContent()
                    : List.of());
        } else {
            model.addAttribute("patients", patientService.search(query, PageRequest.of(0, 10)).getContent());
            model.addAttribute("doctors", doctorService.search(query, PageRequest.of(0, 10)).getContent());
            model.addAttribute("appointments", appointmentService.search(query, PageRequest.of(0, 10)).getContent());
        }

        model.addAttribute("query", query);
        model.addAttribute("activePage", "search");
        return "search-results";
    }
}
