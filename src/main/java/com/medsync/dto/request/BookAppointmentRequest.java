package com.medsync.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookAppointmentRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Start time is required")
    @Future(message = "Appointment must be in the future")
    // @Future — validation catches past bookings before they even hit our service
    private LocalDateTime startTime;
}