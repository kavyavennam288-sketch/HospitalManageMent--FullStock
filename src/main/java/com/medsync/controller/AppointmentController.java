package com.medsync.controller;

import com.medsync.dto.request.BookAppointmentRequest;
import com.medsync.dto.response.AppointmentResponse;
import com.medsync.model.User;
import com.medsync.service.AppointmentService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Book, cancel, and view appointments")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    // POST /api/appointments
    // @AuthenticationPrincipal injects the logged-in User object directly.
    // Spring reads it from SecurityContextHolder — no manual token parsing needed.
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    // @PreAuthorize runs BEFORE the method body — rejects non-patients with 403
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody BookAppointmentRequest request) {

        AppointmentResponse response = appointmentService
            .bookAppointment(currentUser.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // DELETE /api/appointments/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        AppointmentResponse response = appointmentService.cancelAppointment(
            id,
            currentUser.getId(),
            currentUser.getRole().name()
        );

        return ResponseEntity.ok(response);
    }

    // GET /api/appointments/my
    // Patient sees their own appointments
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
            appointmentService.getAppointmentsForPatient(currentUser.getId())
        );
    }

    // GET /api/appointments/doctor
    // Doctor sees their own schedule
    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
            appointmentService.getAppointmentsForDoctor(currentUser.getId())
        );
    }
}