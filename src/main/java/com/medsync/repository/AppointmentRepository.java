// AppointmentRepository.java
package com.medsync.repository;

import com.medsync.model.Appointment;
import com.medsync.model.Appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    // Finds appointments for a doctor within a time window.
    // Used by SlotAllocationService to check if a slot is already booked.
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.startTime < :endTime AND a.endTime > :startTime " +
           "AND a.status != 'CANCELLED'")
    List<Appointment> findConflictingAppointments(
        @Param("doctorId") Long doctorId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // For admin dashboard: count appointments by status
    long countByStatus(AppointmentStatus status);

    // For admin dashboard: all appointments on a specific day
    @Query("SELECT a FROM Appointment a WHERE DATE(a.startTime) = DATE(:date)")
    List<Appointment> findByDate(@Param("date") LocalDateTime date);
}