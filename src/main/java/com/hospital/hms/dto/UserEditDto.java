package com.hospital.hms.dto;

import com.hospital.hms.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Used by an ADMIN to update an existing user's role, enabled status, and
 * (for DOCTOR-role accounts) which clinical Doctor profile the login is
 * linked to — this link is what powers "My Patients" / "My Appointments"
 * scoping for that doctor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEditDto {

    @NotNull(message = "Role is required")
    private Role role;

    private boolean enabled = true;

    /** Only relevant when role == DOCTOR. Null means "not linked yet". */
    private Long doctorId;
}
