package com.hospital.hms.controller;

import com.hospital.hms.dto.PatientDto;
import com.hospital.hms.entity.Gender;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.exception.DuplicateResourceException;
import com.hospital.hms.security.CustomUserDetails;
import com.hospital.hms.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Patient management.
 * - ADMIN: full CRUD, sees every patient.
 * - RECEPTIONIST: can register/edit patients (no delete), sees every patient.
 * - DOCTOR: read-only, and only sees patients assigned to them (i.e. patients
 *   they have at least one appointment with) — this is their "My Patients" view.
 */
@Controller
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    private boolean isDoctor(CustomUserDetails principal) {
        return principal.getUser().getRole() == com.hospital.hms.entity.Role.DOCTOR;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "8") int size,
                        @RequestParam(required = false) String keyword,
                        @AuthenticationPrincipal CustomUserDetails principal,
                        Model model) {
        Page<Patient> patientPage;
        if (isDoctor(principal)) {
            Long doctorId = principal.getDoctorId();
            patientPage = doctorId != null
                    ? patientService.searchForDoctor(doctorId, keyword, PageRequest.of(page, size, Sort.by("id").descending()))
                    : Page.empty();
        } else {
            patientPage = patientService.search(keyword, PageRequest.of(page, size, Sort.by("id").descending()));
        }
        model.addAttribute("patientPage", patientPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "patients");
        return "patients/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public String view(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal, Model model) {
        Patient patient = patientService.getById(id);

        if (isDoctor(principal)) {
            Long doctorId = principal.getDoctorId();
            boolean assigned = doctorId != null && patient.getAppointments().stream()
                    .anyMatch(a -> a.getDoctor() != null && a.getDoctor().getId().equals(doctorId));
            if (!assigned) {
                throw new AccessDeniedException("You can only view patients assigned to you.");
            }
        }

        model.addAttribute("patient", patient);
        model.addAttribute("activePage", "patients");
        return "patients/view";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String newForm(Model model) {
        model.addAttribute("patientDto", new PatientDto());
        model.addAttribute("genders", Gender.values());
        model.addAttribute("activePage", "patients");
        return "patients/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String create(@Valid @ModelAttribute("patientDto") PatientDto dto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("genders", Gender.values());
            model.addAttribute("activePage", "patients");
            return "patients/form";
        }
        try {
            patientService.create(dto);
        } catch (DuplicateResourceException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("genders", Gender.values());
            model.addAttribute("activePage", "patients");
            return "patients/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Patient added successfully.");
        return "redirect:/patients";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String editForm(@PathVariable Long id, Model model) {
        Patient patient = patientService.getById(id);
        PatientDto dto = new PatientDto(patient.getId(), patient.getFirstName(), patient.getLastName(),
                patient.getEmail(), patient.getPhoneNumber(), patient.getDateOfBirth(), patient.getGender(),
                patient.getAddress(), patient.getBloodGroup(), patient.getEmergencyContact());
        model.addAttribute("patientDto", dto);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("activePage", "patients");
        return "patients/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("patientDto") PatientDto dto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("genders", Gender.values());
            model.addAttribute("activePage", "patients");
            return "patients/form";
        }
        try {
            patientService.update(id, dto);
        } catch (DuplicateResourceException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("genders", Gender.values());
            model.addAttribute("activePage", "patients");
            return "patients/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Patient updated successfully.");
        return "redirect:/patients";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        patientService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Patient deleted successfully.");
        return "redirect:/patients";
    }
}
