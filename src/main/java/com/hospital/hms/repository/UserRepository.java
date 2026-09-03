package com.hospital.hms.repository;

import com.hospital.hms.entity.Role;
import com.hospital.hms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRoleOrderByFullNameAsc(Role role);
}
