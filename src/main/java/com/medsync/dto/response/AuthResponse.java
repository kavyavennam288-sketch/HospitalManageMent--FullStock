package com.medsync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;       // the JWT string
    private String tokenType;   // always "Bearer"
    private Long userId;
    private String email;
    private String fullName;
    private String role;        // "PATIENT", "DOCTOR", "ADMIN"
    // The frontend uses 'role' to decide which dashboard to show
}