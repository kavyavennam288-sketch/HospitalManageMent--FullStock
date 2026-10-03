package com.medsync.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rooms")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber;     // "A101", "ICU-3"

    @Enumerated(EnumType.STRING)
    private RoomType type;

    @Builder.Default
    private boolean isAvailable = true;

    public enum RoomType {
        CONSULTATION,
        EMERGENCY,
        ICU,
        OPERATION_THEATER,
        WARD
    }
}