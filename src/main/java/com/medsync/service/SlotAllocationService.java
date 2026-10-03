package com.medsync.service;

import com.medsync.model.Appointment;
import com.medsync.model.Doctor;
import com.medsync.model.Room;
import com.medsync.model.TimeSlot;
import com.medsync.repository.AppointmentRepository;
import com.medsync.repository.RoomRepository;
import com.medsync.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotAllocationService {

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final RoomRepository roomRepository;

    // ── Main Allocation Check ─────────────────────────────────────────
    // Called before every booking attempt.
    // Returns true only when BOTH the doctor AND a room are free.
    // This is the core algorithm that prevents double-bookings.
    public boolean isSlotAvailable(Doctor doctor, LocalDateTime startTime, LocalDateTime endTime) {

        // Guard 1: reject past bookings
        if (startTime.isBefore(LocalDateTime.now())) {
            log.warn("Attempted to book a slot in the past: {}", startTime);
            return false;
        }

        // Guard 2: reject if startTime is after or equal to endTime
        if (!startTime.isBefore(endTime)) {
            log.warn("Invalid time range: start={} end={}", startTime, endTime);
            return false;
        }

        // Check 1: Does this doctor work on this day and time?
        boolean doctorWorksThisSlot = isDoctorAvailableAtTime(
            doctor.getId(),
            startTime.getDayOfWeek(),
            startTime.toLocalTime(),
            endTime.toLocalTime()
        );
        System.out.println(doctorWorksThisSlot);
        if (!doctorWorksThisSlot) {
            log.info("Doctor {} has no schedule for this time slot", doctor.getId());
            return false;
        }

        // Check 2: Does the doctor already have an appointment that overlaps?
        // We use a DB query that checks for ANY overlap, not just exact matches.
        // Overlap condition: existing.start < requested.end AND existing.end > requested.start
        // This catches: partial overlaps, containment, and exact matches.
        List<Appointment> conflicts = appointmentRepository
            .findConflictingAppointments(doctor.getId(), startTime, endTime);

        if (!conflicts.isEmpty()) {
            log.info("Doctor {} has {} conflicting appointment(s) at {}",
                doctor.getId(), conflicts.size(), startTime);
            return false;
        }

        // Check 3: Is at least one consultation room free?
        // (This check is optional for telehealth — we'll handle that in AppointmentService)
        // boolean roomAvailable = isAnyRoomAvailable();
        // if (!roomAvailable) {
        //     log.info("No consultation rooms available at {}", startTime);
        //     return false;
        // }

        return true;
    }

    // ── Get Available Slots for a Doctor on a Specific Date ───────────
    // Returns all time slots for a doctor on a given date that are still open.
    // Used by the frontend to render the booking calendar.
    

public List<TimeSlot> getAvailableSlotsForDoctor(Doctor doctor, LocalDate date) {
    DayOfWeek dayOfWeek = date.getDayOfWeek();  // cleaner — LocalDate has getDayOfWeek() directly

    List<TimeSlot> allSlotsForDay = timeSlotRepository
        .findByDoctorIdAndDayOfWeek(doctor.getId(), dayOfWeek);

    return allSlotsForDay.stream()
        .filter(TimeSlot::isAvailable)
        .filter(slot -> {
            LocalDateTime slotStart = LocalDateTime.of(date, slot.getStartTime()); // clean
            LocalDateTime slotEnd   = LocalDateTime.of(date, slot.getEndTime());

            List<Appointment> conflicts = appointmentRepository
                .findConflictingAppointments(doctor.getId(), slotStart, slotEnd);

            return conflicts.isEmpty();
        })
        .toList();
}

    // ── Find an Available Room ────────────────────────────────────────
    // Returns the first available consultation room.
    // Called by AppointmentService after the slot is confirmed available.
    // public Optional<Room> findAvailableRoom() {
    //     return roomRepository
    //         .findByTypeAndIsAvailableTrue(Room.RoomType.CONSULTATION)
    //         .stream()
    //         .findFirst();
    // }

    // ── Private: Doctor Schedule Check ───────────────────────────────
    // Checks if the doctor has a TimeSlot entry that covers the requested time.
    // A TimeSlot of 09:00–09:30 covers a request for 09:00–09:30 exactly,
    // but NOT a request for 09:00–10:00 (which would spill into the next slot).
    private boolean isDoctorAvailableAtTime(
            Long doctorId,
            DayOfWeek day,
            LocalTime requestedStart,
            LocalTime requestedEnd) {

        List<TimeSlot> slots = timeSlotRepository
            .findByDoctorIdAndDayOfWeek(doctorId, day);

        return slots.stream()
            .filter(TimeSlot::isAvailable)
            .anyMatch(slot ->
                // The slot must start at or before the request
                // AND end at or after the request — fully contains it
                !slot.getStartTime().isAfter(requestedStart) &&
                !slot.getEndTime().isBefore(requestedEnd)
            );
    }

    // ── Private: Any Room Available ───────────────────────────────────
    // private boolean isAnyRoomAvailable() {
    //     return !roomRepository
    //         .findByTypeAndIsAvailableTrue(Room.RoomType.CONSULTATION)
    //         .isEmpty();
    // }
}