package com.medsync.service;

import com.medsync.dto.response.DoctorResponse;
import com.medsync.dto.response.SlotResponse;
import com.medsync.model.Appointment;
import com.medsync.model.Doctor;
import com.medsync.model.TimeSlot;
import com.medsync.repository.AppointmentRepository;
import com.medsync.repository.DoctorRepository;
import com.medsync.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final SlotAllocationService slotAllocationService;
    private final AppointmentRepository appointmentRepository;

    // ── Search Doctors by Specialty ───────────────────────────────────
    // Used on the patient booking page: "Show me all Cardiologists"
    @Transactional(readOnly = true)
    public List<DoctorResponse> getDoctorsBySpecialty(String specialty) {
        return doctorRepository
            .findBySpecialtyAndIsActive(specialty, true)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ── Get All Active Doctors ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findByIsActive(true)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ── Get Available Slots for a Doctor on a Date ────────────────────
    // The frontend calls this when a patient picks a doctor and a date.
    // Returns only the slots that are still open — not already booked.
    

@Transactional(readOnly = true)
public List<SlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
    Doctor doctor = doctorRepository.findById(doctorId)
        .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));

    // Step 1: figure out what day of week this date falls on
    // e.g. 2024-12-25 → WEDNESDAY
    DayOfWeek dayOfWeek = date.getDayOfWeek();

    // Step 2: get all recurring slots the doctor has for that day of week
    List<TimeSlot> recurringSlots = timeSlotRepository
        .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);

    // Step 3: filter out slots that are manually blocked or already booked
    return recurringSlots.stream()
        .filter(TimeSlot::isAvailable)   // doctor hasn't blocked this slot
        .filter(slot -> {
            // Combine the specific DATE the patient picked
            // with the TIME from the recurring slot
            // e.g. date=2024-12-25 + slotStart=09:00 → 2024-12-25T09:00
            LocalDateTime slotStart = LocalDateTime.of(date, slot.getStartTime());
            LocalDateTime slotEnd   = LocalDateTime.of(date, slot.getEndTime());

            // Check: is there already a confirmed/pending appointment in this window?
            List<Appointment> conflicts = appointmentRepository
                .findConflictingAppointments(doctorId, slotStart, slotEnd);

            return conflicts.isEmpty();  // keep slot only if no conflicts
        })
        .map(slot -> mapToSlotResponse(slot, date))  // pass LocalDate here
        .toList();
}

    // ── Add a Time Slot (Doctor manages their own schedule) ───────────
    // Doctors call this to define when they're available.
    // e.g. "I'm available every Monday 9:00–9:30"
    @Transactional
    public SlotResponse addTimeSlot(
            Long doctorId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime) {

        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException(
                "Doctor not found: " + doctorId
            ));

        // Prevent overlapping slot definitions
        List<TimeSlot> existingSlots = timeSlotRepository
            .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);

        boolean overlaps = existingSlots.stream().anyMatch(existing ->
            // Same overlap check as appointment booking — catches all overlap types
            !existing.getStartTime().isAfter(endTime) &&
            !existing.getEndTime().isBefore(startTime)
        );

        if (overlaps) {
            throw new RuntimeException(
                "This time slot overlaps with an existing slot for this doctor"
            );
        }

        TimeSlot slot = TimeSlot.builder()
            .doctor(doctor)
            .dayOfWeek(dayOfWeek)
            .startTime(startTime)
            .endTime(endTime)
            .isAvailable(true)
            .build();

        TimeSlot saved = timeSlotRepository.save(slot);
        log.info("Time slot added for doctor {}: {} {} {}–{}",
            doctorId, dayOfWeek, startTime, endTime, saved.getId());

        // We don't have a date here (it's a recurring weekly slot),
        // so we pass null and handle it in mapToSlotResponse
        return mapToSlotResponse(saved, null);
    }

    // ── Block / Unblock a Time Slot ───────────────────────────────────
    // Doctor blocks a slot: "I'm in surgery this Monday 9:00–9:30, no bookings"
    // Doctor unblocks: "Surgery cancelled, I'm available again"
    @Transactional
    public SlotResponse toggleSlotAvailability(Long slotId, Long doctorId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
            .orElseThrow(() -> new RuntimeException(
                "Time slot not found: " + slotId
            ));

        // Security: a doctor can only toggle their own slots
        if (!slot.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException(
                "You can only modify your own time slots"
            );
        }

        // Flip the availability flag
        slot.setAvailable(!slot.isAvailable());
        TimeSlot saved = timeSlotRepository.save(slot);

        log.info("Slot {} availability toggled to {} by doctor {}",
            slotId, saved.isAvailable(), doctorId);

        return mapToSlotResponse(saved, null);
    }

    // ── Map Entity → Response DTO ─────────────────────────────────────
    private DoctorResponse mapToResponse(Doctor doctor) {
        return DoctorResponse.builder()
            .id(doctor.getId())
            .fullName(doctor.getFullName())
            .email(doctor.getEmail())
            .specialty(doctor.getSpecialty())
            .licenseNumber(doctor.getLicenseNumber())
            .qualifications(doctor.getQualifications())
            .consultationDurationMinutes(doctor.getConsultationFeeMinutes())
            .build();
    }

  

private SlotResponse mapToSlotResponse(TimeSlot slot, LocalDate date) {
    return SlotResponse.builder()
        .id(slot.getId())
        .doctorId(slot.getDoctor().getId())
        .dayOfWeek(slot.getDayOfWeek().name())
        .startTime(slot.getStartTime().toString())
        .endTime(slot.getEndTime().toString())
        .available(slot.isAvailable())
        // Combine the date + slot time into one exact bookable datetime
        // Frontend uses this value directly as the startTime when booking
        .slotDateTime(date != null
            ? LocalDateTime.of(date, slot.getStartTime())  // clean now — no .toLocalDate()
            : null)
        .build();
}
}   