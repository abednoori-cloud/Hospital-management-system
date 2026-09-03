package com.hospital.hms.controller;

import com.hospital.hms.dto.InvoiceDto;
import com.hospital.hms.entity.Invoice;
import com.hospital.hms.entity.PaymentStatus;
import com.hospital.hms.service.DoctorService;
import com.hospital.hms.service.InvoiceService;
import com.hospital.hms.service.PatientService;
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
 * Billing. RECEPTIONIST creates, edits, and prints invoices. ADMIN has
 * read-only access ("View all invoices") plus delete authority for
 * correcting billing mistakes. DOCTOR has no access at all — see
 * SecurityConfig.
 */
@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "8") int size,
                        @RequestParam(required = false) String keyword,
                        Model model) {
        Page<Invoice> invoicePage = invoiceService.search(keyword,
                PageRequest.of(page, size, Sort.by("invoiceDate").descending()));
        model.addAttribute("invoicePage", invoicePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activePage", "invoices");
        return "invoices/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("invoice", invoiceService.getById(id));
        model.addAttribute("activePage", "invoices");
        return "invoices/view";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public String newForm(Model model) {
        model.addAttribute("invoiceDto", new InvoiceDto());
        model.addAttribute("patients", patientService.getAll());
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        model.addAttribute("activePage", "invoices");
        return "invoices/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public String create(@Valid @ModelAttribute("invoiceDto") InvoiceDto dto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("paymentStatuses", PaymentStatus.values());
            return "invoices/form";
        }
        invoiceService.create(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice generated successfully.");
        return "redirect:/invoices";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public String editForm(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.getById(id);
        InvoiceDto dto = new InvoiceDto(invoice.getId(), invoice.getPatient().getId(),
                invoice.getDoctor() != null ? invoice.getDoctor().getId() : null,
                invoice.getInvoiceDate(), invoice.getTreatmentDescription(), invoice.getTreatmentCost(),
                invoice.getPaymentStatus());
        model.addAttribute("invoiceDto", dto);
        model.addAttribute("patients", patientService.getAll());
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        model.addAttribute("activePage", "invoices");
        return "invoices/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("invoiceDto") InvoiceDto dto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("patients", patientService.getAll());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("paymentStatuses", PaymentStatus.values());
            return "invoices/form";
        }
        invoiceService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice updated successfully.");
        return "redirect:/invoices";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        invoiceService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice deleted successfully.");
        return "redirect:/invoices";
    }
}
