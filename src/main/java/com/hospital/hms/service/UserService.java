package com.hospital.hms.service;

import com.hospital.hms.dto.RegisterDto;
import com.hospital.hms.dto.UserEditDto;
import com.hospital.hms.entity.Role;
import com.hospital.hms.entity.User;

import java.util.List;

public interface UserService {

    User register(RegisterDto dto);

    List<User> getAll();

    List<User> getByRole(Role role);

    User getById(Long id);

    User update(Long id, UserEditDto dto);

    void delete(Long id);
}
