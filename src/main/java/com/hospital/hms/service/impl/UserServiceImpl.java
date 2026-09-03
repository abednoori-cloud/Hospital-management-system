package com.hospital.hms.service.impl;

import com.hospital.hms.dto.RegisterDto;
import com.hospital.hms.dto.UserEditDto;
import com.hospital.hms.entity.Doctor;
import com.hospital.hms.entity.Role;
import com.hospital.hms.entity.User;
import com.hospital.hms.exception.DuplicateResourceException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.UserRepository;
import com.hospital.hms.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterDto dto) {
        if (dto.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("Administrator accounts cannot be self-registered.");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("User", "username", dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User", "email", dto.getEmail());
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user '{}' with role {}", saved.getUsername(), saved.getRole());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getByRole(Role role) {
        return userRepository.findByRoleOrderByFullNameAsc(role);
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    public User update(Long id, UserEditDto dto) {
        User user = getById(id);
        user.setRole(dto.getRole());
        user.setEnabled(dto.isEnabled());

        if (dto.getRole() == Role.DOCTOR && dto.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", dto.getDoctorId()));
            user.setDoctorProfile(doctor);
        } else {
            user.setDoctorProfile(null);
        }

        log.info("Updated user with id {} (role={}, enabled={})", id, user.getRole(), user.isEnabled());
        return userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        User user = getById(id);
        userRepository.delete(user);
        log.info("Deleted user with id {}", id);
    }
}
