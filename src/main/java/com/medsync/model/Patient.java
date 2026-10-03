package com.medsync.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "patients")
@PrimaryKeyJoinColumn(name = "id")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Patient extends User {

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;      // A_POS, B_NEG, O_POS, etc.

    private String allergies;           // free text, comma-separated
    private String emergencyContact;    // name + phone number

    // Enum nested here since it's only used by Patient
    public enum BloodGroup {
        A_POS, A_NEG, B_POS, B_NEG, AB_POS, AB_NEG, O_POS, O_NEG
    }
}