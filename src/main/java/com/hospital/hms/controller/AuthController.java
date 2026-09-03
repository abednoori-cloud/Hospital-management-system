package com.hospital.hms.controller;

import com.hospital.hms.dto.RegisterDto;
import com.hospital.hms.entity.Role;
import com.hospital.hms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Public self-registration only offers non-privileged roles. ADMIN
     * accounts are never created through the public form — they exist via
     * the seeded default admin, or by an existing ADMIN promoting an
     * account's role from the Users management screen.
     */
    private static final Role[] SELF_REGISTER_ROLES = {Role.DOCTOR, Role.RECEPTIONIST};

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                             @RequestParam(value = "logout", required = false) String logout,
                             Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        model.addAttribute("roles", SELF_REGISTER_ROLES);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDto") RegisterDto registerDto,
                            BindingResult result,
                            Model model) {
        if (registerDto.getRole() == Role.ADMIN) {
            result.rejectValue("role", "invalid.role", "Administrator accounts cannot be self-registered.");
        }
        if (result.hasErrors()) {
            model.addAttribute("roles", SELF_REGISTER_ROLES);
            return "auth/register";
        }
        try {
            userService.register(registerDto);
        } catch (com.hospital.hms.exception.DuplicateResourceException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("roles", SELF_REGISTER_ROLES);
            return "auth/register";
        }
        model.addAttribute("successMessage", "Registration successful. Please log in.");
        return "auth/login";
    }
}
