package com.hospital.hms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ADMIN-only hospital settings screen. Currently read-only/informational —
 * a natural extension point for persisted hospital-wide configuration
 * (name, address, working hours, departments, etc.) in a future iteration.
 */
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController {

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("activePage", "settings");
        return "settings";
    }
}
