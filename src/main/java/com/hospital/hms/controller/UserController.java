package com.hospital.hms.controller;

import com.hospital.hms.dto.UserEditDto;
import com.hospital.hms.entity.Role;
import com.hospital.hms.entity.User;
import com.hospital.hms.service.DoctorService;
import com.hospital.hms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * ADMIN-only management of login accounts (users). Also serves the
 * "Receptionists" admin nav item via an optional ?role= filter, since
 * receptionist accounts are just Users with role=RECEPTIONIST.
 */
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final DoctorService doctorService;

    @GetMapping
    public String list(@RequestParam(required = false) Role role, Model model) {
        List<User> users = role != null ? userService.getByRole(role) : userService.getAll();
        model.addAttribute("users", users);
        model.addAttribute("selectedRole", role);
        model.addAttribute("activePage", role == Role.RECEPTIONIST ? "receptionists" : "users");
        return "users/list";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.getById(id);
        UserEditDto dto = new UserEditDto(user.getRole(), user.isEnabled(),
                user.getDoctorProfile() != null ? user.getDoctorProfile().getId() : null);
        model.addAttribute("user", user);
        model.addAttribute("userEditDto", dto);
        model.addAttribute("roles", Role.values());
        model.addAttribute("doctors", doctorService.getAll());
        model.addAttribute("activePage", "users");
        return "users/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("userEditDto") UserEditDto dto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("user", userService.getById(id));
            model.addAttribute("roles", Role.values());
            model.addAttribute("doctors", doctorService.getAll());
            model.addAttribute("activePage", "users");
            return "users/form";
        }
        userService.update(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        return "redirect:/users";
    }
}
