package com.medsync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    private Long id;
    private String fullName;
    private String email;
    private LocalDate dateOfBirth;
    private String bloodGroup;
    private String allergies;
    private String emergencyContact;
    private boolean active;          // false = soft-deleted
    private LocalDateTime createdAt;
}