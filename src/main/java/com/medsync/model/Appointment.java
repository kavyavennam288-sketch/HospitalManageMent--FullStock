package com.medsync.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments",
    // Composite index on doctor + start time → makes
    // "find all appointments for Dr. X on date Y" fast
    indexes = {
        @Index(name = "idx_appointment_doctor_time",
               columnList = "doctor_id, start_time"),
        @Index(name = "idx_appointment_patient",
               columnList = "patient_id")
    }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne → many appointments can belong to one patient
    // @JoinColumn → creates patient_id FK column in the appointments table
    // fetch = LAZY → don't load Patient from DB until appointment.getPatient() is called
    //               (EAGER would load the entire Patient object on every appointment query)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column
    private String roomNumber;         // nullable — telehealth appointments have no room

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    private String notes;        // doctor's notes after consultation
    private String cancellationReason;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum AppointmentStatus {
        PENDING,     // booked, awaiting confirmation
        CONFIRMED,   // doctor confirmed
        COMPLETED,   // consultation done
        CANCELLED    // by patient or doctor
    }
}