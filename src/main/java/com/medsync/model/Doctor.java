package com.medsync.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "doctors")
// @PrimaryKeyJoinColumn tells JPA: "the 'id' column in the doctors table
// is a foreign key pointing to users.id — not its own auto-increment".
// This is how JOINED inheritance links the tables.
@PrimaryKeyJoinColumn(name = "id")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Doctor extends User {

    private String specialty;          // e.g. "Cardiology", "Orthopedics"
    private String licenseNumber;      // unique medical license
    private String qualifications;     // "MBBS, MD - Internal Medicine"
    private Integer consultationFeeMinutes; // appointment slot duration

    // One doctor → many available time slots
    // mappedBy = "doctor" means TimeSlot has a @ManyToOne field called 'doctor'
    // cascade = ALL → saving a Doctor also saves their TimeSlots
    // orphanRemoval → deleting a slot from this list removes it from DB
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeSlot> availableSlots;
}