package com.medsync.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resources")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;           // "ICU Beds", "Ventilators", "MRI Machine"

    private int totalCount;        // how many the hospital has
    private int usedCount;         // how many are currently occupied

    // Computed — not stored in DB, derived from the two above
    // @Transient tells JPA: don't map this to a column
    @Transient
    public int getAvailableCount() {
        return totalCount - usedCount;
    }

    @Transient
    public double getOccupancyRate() {
        if (totalCount == 0) return 0;
        return (double) usedCount / totalCount * 100;
    }
}