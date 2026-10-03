package com.medsync.dto.request;

import com.medsync.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data  // Lombok: @Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private Role role;           // PATIENT, DOCTOR, or ADMIN

    // Doctor-specific — nullable for patients and admins
    private String specialty;
    private String licenseNumber;
}