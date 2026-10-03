package com.medsync.controller;

import com.medsync.dto.response.DoctorResponse;
import com.medsync.dto.response.SlotResponse;
import com.medsync.model.User;
import com.medsync.service.DoctorService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Search doctors, view and manage schedules")
@SecurityRequirement(name = "bearerAuth")
public class DoctorController {

    private final DoctorService doctorService;

    // GET /api/doctors
    // Public — no auth needed (permitted in SecurityConfig)
    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    // GET /api/doctors?specialty=Cardiology
    @GetMapping("/search")
    public ResponseEntity<List<DoctorResponse>> getDoctorsBySpecialty(
            @RequestParam String specialty) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialty(specialty));
    }

    // GET /api/doctors/{id}/slots?date=2024-12-25T00:00:00
    // Returns available slots for a specific doctor on a specific date
    

@GetMapping("/{id}/slots")
public ResponseEntity<List<SlotResponse>> getAvailableSlots(
        @PathVariable Long id,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // ISO.DATE format = "2024-12-25"  (no time part needed from user)

    return ResponseEntity.ok(doctorService.getAvailableSlots(id, date));
}

    // POST /api/doctors/schedule/slots
    // Doctor adds a recurring weekly time slot to their schedule
    @PostMapping("/schedule/slots")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<SlotResponse> addTimeSlot(
            @AuthenticationPrincipal User currentUser,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {

        return ResponseEntity.ok(
            doctorService.addTimeSlot(
                currentUser.getId(), dayOfWeek, startTime, endTime
            )
        );
    }

    // PUT /api/doctors/schedule/slots/{slotId}/toggle
    // Doctor blocks or unblocks a specific slot
    @PutMapping("/schedule/slots/{slotId}/toggle")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<SlotResponse> toggleSlot(
            @PathVariable Long slotId,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
            doctorService.toggleSlotAvailability(slotId, currentUser.getId())
        );
    }
}