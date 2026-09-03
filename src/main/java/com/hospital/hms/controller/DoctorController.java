package com.hospital.hms.controller;

import com.hospital.hms.dto.DoctorDto;
import com.hospital.hms.entity.Doctor;
import com.hospital.hms.exception.DuplicateResourceException;
import com.hospital.hms.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Doctor directory (clinical profiles). Only ADMIN can create, edit, or
 * delete doctor profiles. RECEPTIONIST has read-only access (needed to pick
 * a doctor when booking an appointment). DOCTOR has no access to this
 * module — see SecurityConfig.
 */
@Controller
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "8") int size,
                        @RequestParam(required = false) String keyword,
                        Model model) {
        Page<Doctor> doctorPage = doctorService.search(keyword,
                PageRequest.of(page, size, Sort.by("id").descending()));
        model.addAttribute("doctorPage", doctorPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "doctors");
        return "doctors/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("doctor", doctorService.getById(id));
        model.addAttribute("activePage", "doctors");
        return "doctors/view";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("doctorDto", new DoctorDto());
        model.addAttribute("activePage", "doctors");
        return "doctors/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid @ModelAttribute("doctorDto") DoctorDto dto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "doctors");
            return "doctors/form";
        }
        try {
            doctorService.create(dto);
        } catch (DuplicateResourceException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activePage", "doctors");
            return "doctors/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Doctor added successfully.");
        return "redirect:/doctors";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getById(id);
        DoctorDto dto = new DoctorDto(doctor.getId(), doctor.getFirstName(), doctor.getLastName(),
                doctor.getEmail(), doctor.getPhoneNumber(), doctor.getSpecialization(), doctor.getDepartment(),
                doctor.getYearsOfExperience(), doctor.getConsultationFee(), doctor.isAvailable());
        model.addAttribute("doctorDto", dto);
        model.addAttribute("activePage", "doctors");
        return "doctors/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("doctorDto") DoctorDto dto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "doctors");
            return "doctors/form";
        }
        try {
            doctorService.update(id, dto);
        } catch (DuplicateResourceException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("activePage", "doctors");
            return "doctors/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Doctor updated successfully.");
        return "redirect:/doctors";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        doctorService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Doctor deleted successfully.");
        return "redirect:/doctors";
    }
}
