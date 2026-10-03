package com.medsync.controller;

import com.medsync.dto.request.LoginRequest;
import com.medsync.dto.request.RegisterRequest;
import com.medsync.dto.response.AuthResponse;
import com.medsync.service.AuthService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// @RestController = @Controller + @ResponseBody
// Every method return value is automatically serialised to JSON
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints") 
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    // @Valid triggers the validation annotations on RegisterRequest
    // (@NotBlank, @Email, @Size). If any fail, Spring returns 400 automatically.
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        // 201 Created is the correct HTTP status for a successful resource creation
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        // 200 OK — the resource (session/token) already existed logically
        return ResponseEntity.ok(response);
    }
}