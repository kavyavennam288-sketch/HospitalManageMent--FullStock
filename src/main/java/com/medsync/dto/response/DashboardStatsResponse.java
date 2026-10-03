package com.medsync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // ── Appointment stats ─────────────────────────────────────────────
    private long totalAppointments;
    private long confirmedAppointments;
    private long pendingAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private int todaysAppointmentCount;

    // ── User stats ────────────────────────────────────────────────────
    private long totalActivePatients;
    private long totalActiveDoctors;

    // ── Resource stats ────────────────────────────────────────────────
    // Map for simple occupancy chart: { "ICU Beds": 75.0 }
    private Map<String, Double> resourceOccupancy;

    // List for detailed stacked bar chart
    private List<ResourceStat> resourceStats;

    // ── Nested DTO ────────────────────────────────────────────────────
    // Defined as a static inner class — keeps it co-located with its parent DTO.
    // The frontend maps this directly to Recharts BarChart data format.
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceStat {
        private String name;          // "ICU Beds"
        private int total;            // 10
        private int used;             // 7
        private int available;        // 3
        private double occupancyRate; // 70.0
    }
}