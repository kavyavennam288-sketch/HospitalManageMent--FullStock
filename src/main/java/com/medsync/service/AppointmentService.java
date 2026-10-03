package com.medsync.service;

import com.medsync.dto.request.BookAppointmentRequest;
import com.medsync.dto.response.AppointmentResponse;
import com.medsync.model.*;
import com.medsync.repository.AppointmentRepository;
import com.medsync.repository.DoctorRepository;
import com.medsync.repository.PatientRepository;
import com.medsync.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ResourceRepository resourceRepository;
    private final SlotAllocationService slotAllocationService;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    public AppointmentResponse bookAppointment(Long patientId, BookAppointmentRequest request) {

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new RuntimeException(
                "Doctor not found: " + request.getDoctorId()));

        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException(
                "Patient not found: " + patientId));

        LocalDateTime startTime = request.getStartTime();
        int durationMinutes = doctor.getConsultationFeeMinutes() != null
            ? doctor.getConsultationFeeMinutes() : 30;
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        if (!slotAllocationService.isSlotAvailable(doctor, startTime, endTime)) {
            throw new RuntimeException(
                "The requested slot is not available. Please choose another time.");
        }

        Appointment appointment = Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .startTime(startTime)
            .endTime(endTime)
            .status(Appointment.AppointmentStatus.CONFIRMED)
            .build();

        // Save first to get the generated ID
        Appointment saved = appointmentRepository.save(appointment);

        // Auto-assign a room number using the appointment ID
        // Simple, deterministic, no room table needed
        // Cycles through ROOM-1 to ROOM-10 based on appointment ID
        String roomNumber = "ROOM-" + ((saved.getId() % 10) + 1);
        saved.setRoomNumber(roomNumber);
        saved = appointmentRepository.save(saved);

        // ── Update resource counts ─────────────────────────────────
        // Increment "Consultation Rooms" resource used count if it exists
        // This links the booking flow to the admin dashboard automatically
        incrementResourceIfExists("Consultation Rooms");

        emailNotificationService.sendBookingConfirmation(saved);

        log.info("Appointment {} booked — patient {} with doctor {} in {}",
            saved.getId(), patientId, doctor.getId(), roomNumber);

        return mapToResponse(saved);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(
            Long appointmentId, Long requestingUserId, String requestingUserRole) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException(
                "Appointment not found: " + appointmentId));

        boolean isAdmin = "ADMIN".equals(requestingUserRole);
        boolean isOwner = appointment.getPatient().getId().equals(requestingUserId);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                "You are not authorized to cancel this appointment");
        }

        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed appointment");
        }
        if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Appointment is already cancelled");
        }

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setCancellationReason("Cancelled by " + requestingUserRole.toLowerCase());

        Appointment cancelled = appointmentRepository.save(appointment);

        // ── Free up resource when appointment is cancelled ─────────
        decrementResourceIfExists("Consultation Rooms");

        emailNotificationService.sendCancellationNotification(cancelled);

        return mapToResponse(cancelled);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsForPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId)
            .stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsForDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId)
            .stream().map(this::mapToResponse).toList();
    }

    // ── Resource Helpers ───────────────────────────────────────────────
    // Silently skip if the resource doesn't exist yet —
    // admin creates resources independently, booking never fails because of this

    private void incrementResourceIfExists(String resourceName) {
        resourceRepository.findAll().stream()
            .filter(r -> r.getName().equalsIgnoreCase(resourceName))
            .findFirst()
            .ifPresent(r -> {
                if (r.getUsedCount() < r.getTotalCount()) {
                    r.setUsedCount(r.getUsedCount() + 1);
                    resourceRepository.save(r);
                    log.info("Resource '{}' incremented to {}/{}",
                        r.getName(), r.getUsedCount(), r.getTotalCount());
                }
            });
    }

    private void decrementResourceIfExists(String resourceName) {
        resourceRepository.findAll().stream()
            .filter(r -> r.getName().equalsIgnoreCase(resourceName))
            .findFirst()
            .ifPresent(r -> {
                if (r.getUsedCount() > 0) {
                    r.setUsedCount(r.getUsedCount() - 1);
                    resourceRepository.save(r);
                    log.info("Resource '{}' decremented to {}/{}",
                        r.getName(), r.getUsedCount(), r.getTotalCount());
                }
            });
    }

    // ── Map Entity → Response ─────────────────────────────────────────
    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
            .id(appointment.getId())
            .patientId(appointment.getPatient().getId())
            .patientName(appointment.getPatient().getFullName())
            .doctorId(appointment.getDoctor().getId())
            .doctorName(appointment.getDoctor().getFullName())
            .doctorSpecialty(appointment.getDoctor().getSpecialty())
            // Room is now just a string field — no join needed
            .roomNumber(appointment.getRoomNumber() != null
                ? appointment.getRoomNumber() : "ROOM-1")
            .startTime(appointment.getStartTime())
            .endTime(appointment.getEndTime())
            .status(appointment.getStatus().name())
            .notes(appointment.getNotes())
            .createdAt(appointment.getCreatedAt())
            .build();
    }
}