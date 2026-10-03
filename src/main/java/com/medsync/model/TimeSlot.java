package com.medsync.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "time_slots")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;   // MONDAY, TUESDAY … — Java built-in enum

    private LocalTime startTime;   // e.g. 09:00
    private LocalTime endTime;     // e.g. 09:30

    @Builder.Default
    private boolean isAvailable = true;
    // Set to false when doctor blocks out this slot (vacation, surgery, etc.)
}