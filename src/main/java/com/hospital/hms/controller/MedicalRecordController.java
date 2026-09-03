package com.hospital.hms.controller;

import com.hospital.hms.dto.MedicalRecordDto;
import com.hospital.hms.entity.MedicalRecord;
import com.hospital.hms.entity.Role;
import com.hospital.hms.security.CustomUserDetails;
import com.hospital.hms.service.DoctorService;
import com.hospital.hms.service.MedicalRecordService;
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

import java.util.List;

/**
 * Medical records hold clinical diagnoses and prescriptions.
 * - RECEPTIONIST: no access at all (blocked entirely at SecurityConfig level).
 * - ADMIN: read-only ("View all medical records") — cannot write a diagnosis
 *   or prescription, so create/update/delete are DOCTOR-only.
 * - DOCTOR: full CRUD, but only on their OWN records; the doctor field on
 *   the form is locked to themselves rather than an open dropdown.
 */
@Controller
@RequestMapping("/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    private boolean isDoctor(CustomUserDetails principal) {
        return principal.getUser().getRole() == Role.DOCTOR;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "8") int size,
                        @RequestParam(required = false) String keyword,
                        @AuthenticationPrincipal CustomUserDetails principal,
                        Model model) {
        Page<MedicalRecord> recordPage;
        if (isDoctor(principal)) {
            Long doctorId = principal.getDoctorId();
            recordPage = doctorId != null
                    ? medicalRecordService.searchForDoctor(doctorId, keyword, PageRequest.of(page, size, Sort.by("visitDate").descending()))
                    : Page.empty();
        } else {
            recordPage = medicalRecordService.search(keyword, PageRequest.of(page, size, Sort.by("visitDate").descending()));
        }
        model.addAttribute("recordPage", recordPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "medical-records");
        return "medical-records/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('DOCTOR')")
    public String newForm(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setDoctorId(principal.getDoctorId());
        model.addAttribute("medicalRecordDto", dto);
        model.addAttribute("patients", patientService.getAll());
        model.addAttribute("doctors", ownDoctorOnly(principal));
        model.addAttribute("activePage", "medical-records");
        return "medical-records/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public String create(@Valid @ModelAttribute("medicalRecordDto") MedicalRecordDto dto,
                          BindingResult result, @AuthenticationPrincipal CustomUserDetails principal, Model model,
                          RedirectAttributes redirectAttributes) {
        enforceOwnDoctor(dto, principal);
        if (result.hasErrors()) {
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("doctors", ownDoctorOnly(principal));
            model.addAttribute("activePage", "medical-records");
            return "medical-records/form";
        }
        medicalRecordService.create(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Medical record added successfully.");
        return "redirect:/medical-records";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('DOCTOR')")
    public String editForm(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal, Model model) {
        MedicalRecord record = medicalRecordService.getById(id);
        assertOwnership(record, principal);

        MedicalRecordDto dto = new MedicalRecordDto(record.getId(), record.getPatient().getId(),
                record.getDoctor().getId(), record.getVisitDate(), record.getDiagnosis(),
                record.getPrescription(), record.getDoctorNotes());
        model.addAttribute("medicalRecordDto", dto);
        model.addAttribute("patients", patientService.getAll());
        model.addAttribute("doctors", ownDoctorOnly(principal));
        model.addAttribute("activePage", "medical-records");
        return "medical-records/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("medicalRecordDto") MedicalRecordDto dto,
                          BindingResult result, @AuthenticationPrincipal CustomUserDetails principal, Model model,
                          RedirectAttributes redirectAttributes) {
        assertOwnership(medicalRecordService.getById(id), principal);
        enforceOwnDoctor(dto, principal);
        if (result.hasErrors()) {
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("doctors", ownDoctorOnly(principal));
            model.addAttribute("activePage", "medical-records");
            return "medical-records/form";
        }
        medicalRecordService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Medical record updated successfully.");
        return "redirect:/medical-records";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('DOCTOR')")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal,
                          RedirectAttributes redirectAttributes) {
        assertOwnership(medicalRecordService.getById(id), principal);
        medicalRecordService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Medical record deleted successfully.");
        return "redirect:/medical-records";
    }

    private List<com.hospital.hms.entity.Doctor> ownDoctorOnly(CustomUserDetails principal) {
        Long doctorId = principal.getDoctorId();
        return doctorId != null ? List.of(doctorService.getById(doctorId)) : List.of();
    }

    private void enforceOwnDoctor(MedicalRecordDto dto, CustomUserDetails principal) {
        Long doctorId = principal.getDoctorId();
        if (doctorId == null || dto.getDoctorId() == null || !dto.getDoctorId().equals(doctorId)) {
            throw new AccessDeniedException("You can only create or edit your own medical records.");
        }
    }

    private void assertOwnership(MedicalRecord record, CustomUserDetails principal) {
        Long doctorId = principal.getDoctorId();
        if (doctorId == null || !record.getDoctor().getId().equals(doctorId)) {
            throw new AccessDeniedException("You can only manage your own medical records.");
        }
    }
}
