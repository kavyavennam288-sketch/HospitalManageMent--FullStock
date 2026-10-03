package com.medsync.service;

import com.medsync.dto.request.LoginRequest;
import com.medsync.dto.request.RegisterRequest;
import com.medsync.dto.response.AuthResponse;
import com.medsync.model.Doctor;
import com.medsync.model.Patient;
import com.medsync.model.Role;
import com.medsync.model.User;
import com.medsync.repository.UserRepository;
import com.medsync.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // ── Register ──────────────────────────────────────────────────────
    @Transactional
    // @Transactional wraps the whole method in a DB transaction.
    // If anything fails (e.g. duplicate email), the whole operation rolls back.
    public AuthResponse register(RegisterRequest request) {

        // Guard against duplicate registrations
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered: " + request.getEmail());
        }

        // Build the correct subtype based on the role in the request.
        // We use the Builder pattern (from Lombok @Builder on User/Doctor/Patient).
        User user;

        if (request.getRole() == Role.DOCTOR) {
            // Doctor.builder() inherits all User fields + adds Doctor-specific ones
            user = Doctor.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                // CRITICAL: never store raw passwords.
                // BCryptPasswordEncoder.encode() produces: "$2a$10$..."
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DOCTOR)
                .specialty(request.getSpecialty())
                .licenseNumber(request.getLicenseNumber())
                .build();

        } else if (request.getRole() == Role.PATIENT) {
            user = Patient.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PATIENT)
                .build();

        } else {
            // ADMIN — plain User with ADMIN role (no subclass needed)
            user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build();
        }

        // JPA INSERT — Spring Data JPA generates the SQL, handles ID assignment
        User savedUser = userRepository.save(user);

        // Issue a token immediately so the user is logged in after registering
        String token = jwtTokenProvider.generateToken(savedUser);
        return buildAuthResponse(savedUser, token);
    }

    // ── Login ─────────────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        // authenticationManager.authenticate() does two things:
        // 1. Calls CustomUserDetailsService.loadUserByUsername(email) to fetch the User
        // 2. Calls passwordEncoder.matches(rawPassword, hashedPassword) to verify
        // If either step fails, it throws BadCredentialsException → 401
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // Store authentication in SecurityContext for the duration of this request
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // The principal is our User object (since User implements UserDetails)
        User user = (User) authentication.getPrincipal();

        // Generate and return the JWT
        String token = jwtTokenProvider.generateToken(user);
        return buildAuthResponse(user, token);
    }

    // ── Helper ────────────────────────────────────────────────────────
    // Builds the response DTO — what the frontend receives after login/register.
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .userId(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole() != null ? user.getRole().name() : null)
            .build();
    }
}