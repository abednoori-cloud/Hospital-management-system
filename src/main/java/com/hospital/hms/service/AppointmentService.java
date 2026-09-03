package com.hospital.hms.service;

import com.hospital.hms.dto.AppointmentDto;
import com.hospital.hms.entity.Appointment;
import com.hospital.hms.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AppointmentService {

    Appointment book(AppointmentDto dto);

    Appointment update(Long id, AppointmentDto dto);

    void cancel(Long id);

    void complete(Long id);

    void delete(Long id);

    Appointment getById(Long id);

    List<Appointment> getAll();

    Page<Appointment> getPage(Pageable pageable);

    Page<Appointment> search(String keyword, Pageable pageable);

    Page<Appointment> getPageForDoctor(Long doctorId, Pageable pageable);

    Page<Appointment> searchForDoctor(Long doctorId, String keyword, Pageable pageable);

    List<Appointment> getByStatus(AppointmentStatus status);

    List<Appointment> getRecent();

    long countAll();

    long countToday();

    long countTodayForDoctor(Long doctorId);

    long countCompletedForDoctor(Long doctorId);
}
