package com.hospital.hms.controller;

import com.hospital.hms.dto.AppointmentDto;
import com.hospital.hms.entity.Appointment;
import com.hospital.hms.entity.AppointmentStatus;
import com.hospital.hms.entity.Role;
import com.hospital.hms.security.CustomUserDetails;
import jakarta.persistence.EntityExistsException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import java.util.Arrays;

import com.hospital.hms.service.AppointmentService;
import com.hospital.hms.service.DoctorService;
import com.hospital.hms.service.PatientService;

/**
 * Appointment management.
 * - ADMIN: sees and manages every appointment.
 * - RECEPTIONIST: books, edits, and cancels appointments (no delete beyond admin).
 * - DOCTOR: read-only on their OWN appointments ("View own appointments"),
 *   plus the ability to mark one of their appointments as completed via the
 *   dedicated /complete action.
 *
 * NOTE: ADMIN/RECEPTIONIST can never set an appointment's status to COMPLETED
 * through the book/edit form — that transition is DOCTOR-only, enforced both
 * by filtering it out of the status dropdown (EDITABLE_STATUSES) and by a
 * server-side check in create()/update() that rejects it outright.
 */
@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    private static final AppointmentStatus[] EDITABLE_STATUSES = Arrays.stream(AppointmentStatus.values())
            .filter(s -> s != AppointmentStatus.COMPLETED)
            .toArray(AppointmentStatus[]::new);

    private boolean isDoctor(CustomUserDetails principal) {
        return principal.getUser().getRole() == Role.DOCTOR;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "8") int size,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) AppointmentStatus status,
                        @AuthenticationPrincipal CustomUserDetails principal,
                        Model model) {
        Page<Appointment> appointmentPage;

        if (isDoctor(principal)) {
            Long doctorId = principal.getDoctorId();
            if (doctorId == null) {
                appointmentPage = Page.empty();
            } else if (status != null) {
                var list = appointmentService.getByStatus(status).stream()
                        .filter(a -> a.getDoctor().getId().equals(doctorId))
                        .toList();
                appointmentPage = new PageImpl<>(list);
            } else {
                appointmentPage = appointmentService.searchForDoctor(doctorId, keyword,
                        PageRequest.of(page, size, Sort.by("appointmentDateTime").descending()));
            }
        } else if (status != null) {
            var list = appointmentService.getByStatus(status);
            appointmentPage = new PageImpl<>(list);
        } else {
            appointmentPage = appointmentService.search(keyword,
                    PageRequest.of(page, size, Sort.by("appointmentDateTime").descending()));
        }

        model.addAttribute("appointmentPage", appointmentPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statuses", AppointmentStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("activePage", "appointments");
        return "appointments/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String newForm(Model model) {
        model.addAttribute("appointmentDto", new AppointmentDto());
        model.addAttribute("patients", patientService.getAll());
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("statuses", EDITABLE_STATUSES);
        model.addAttribute("activePage", "appointments");
        return "appointments/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String create(@Valid @ModelAttribute("appointmentDto") AppointmentDto dto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttributes) {
        rejectCompletedStatus(dto, result);
        if (result.hasErrors()) {
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("statuses", EDITABLE_STATUSES);
            model.addAttribute("activePage", "appointments");
            return "appointments/form";
        }
        try {
            appointmentService.book(dto);
        } catch (EntityExistsException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("statuses", EDITABLE_STATUSES);
            model.addAttribute("activePage", "appointments");
            return "appointments/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Appointment booked successfully.");
        return "redirect:/appointments";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String editForm(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentService.getById(id);
        AppointmentDto dto = new AppointmentDto(appointment.getId(), appointment.getPatient().getId(),
                appointment.getDoctor().getId(), appointment.getAppointmentDateTime(), appointment.getReason(),
                appointment.getStatus(), appointment.getNotes());
        model.addAttribute("appointmentDto", dto);
        model.addAttribute("patients", patientService.getAll());
        model.addAttribute("doctors", doctorService.getAll());
        // Include the current status even if it's COMPLETED so the select box has a
        // valid selected option to display; the rejectCompletedStatus() guard below
        // still blocks changing *into* COMPLETED through this form.
        model.addAttribute("statuses", appointment.getStatus() == AppointmentStatus.COMPLETED
                ? AppointmentStatus.values() : EDITABLE_STATUSES);
        model.addAttribute("activePage", "appointments");
        return "appointments/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("appointmentDto") AppointmentDto dto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttributes) {
        Appointment existing = appointmentService.getById(id);
        // Allow no-op (status unchanged) even if it's already COMPLETED; only block
        // ADMIN/RECEPTIONIST from newly setting COMPLETED via this generic form.
        if (dto.getStatus() != existing.getStatus()) {
            rejectCompletedStatus(dto, result);
        }
        if (result.hasErrors()) {
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("statuses", existing.getStatus() == AppointmentStatus.COMPLETED
                    ? AppointmentStatus.values() : EDITABLE_STATUSES);
            model.addAttribute("activePage", "appointments");
            return "appointments/form";
        }
        try {
            appointmentService.update(id, dto);
        } catch (EntityExistsException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("statuses", EDITABLE_STATUSES);
            model.addAttribute("activePage", "appointments");
            return "appointments/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Appointment updated successfully.");
        return "redirect:/appointments";
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        appointmentService.cancel(id);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled.");
        return "redirect:/appointments";
    }

    /** DOCTOR marks one of their own appointments as completed after the visit. */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public String complete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal,
                            RedirectAttributes redirectAttributes) {
        Appointment appointment = appointmentService.getById(id);
        Long doctorId = principal.getDoctorId();
        if (doctorId == null || !appointment.getDoctor().getId().equals(doctorId)) {
            throw new AccessDeniedException("You can only complete your own appointments.");
        }
        appointmentService.complete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment marked as completed.");
        return "redirect:/appointments";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        appointmentService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment deleted successfully.");
        return "redirect:/appointments";
    }

    private void rejectCompletedStatus(AppointmentDto dto, BindingResult result) {
        if (dto.getStatus() == AppointmentStatus.COMPLETED) {
            result.rejectValue("status", "invalid.status",
                    "Only the assigned doctor can mark an appointment as completed.");
        }
    }
}
