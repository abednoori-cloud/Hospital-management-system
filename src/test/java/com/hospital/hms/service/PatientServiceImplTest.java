package com.hospital.hms.service;

import com.hospital.hms.dto.PatientDto;
import com.hospital.hms.entity.Gender;
import com.hospital.hms.entity.Patient;
import com.hospital.hms.exception.DuplicateResourceException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private PatientDto patientDto;

    @BeforeEach
    void setUp() {
        patientDto = new PatientDto();
        patientDto.setFirstName("Jane");
        patientDto.setLastName("Doe");
        patientDto.setEmail("jane.doe@example.com");
        patientDto.setPhoneNumber("+1-555-0100");
        patientDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patientDto.setGender(Gender.FEMALE);
    }

    @Test
    void create_savesPatient_whenEmailAndPhoneAreUnique() {
        when(patientRepository.existsByEmail(patientDto.getEmail())).thenReturn(false);
        when(patientRepository.existsByPhoneNumber(patientDto.getPhoneNumber())).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        Patient result = patientService.create(patientDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("Jane Doe");
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void create_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        when(patientRepository.existsByEmail(patientDto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> patientService.create(patientDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(patientDto.getEmail());

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void getById_throwsResourceNotFoundException_whenPatientDoesNotExist() {
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
