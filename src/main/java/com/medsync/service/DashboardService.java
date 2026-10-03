package com.medsync.service;

import com.medsync.dto.response.DashboardStatsResponse;
import com.medsync.model.Appointment.AppointmentStatus;
import com.medsync.repository.AppointmentRepository;
import com.medsync.repository.DoctorRepository;
import com.medsync.repository.PatientRepository;
import com.medsync.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ResourceRepository resourceRepository;

    // ── Main Stats Aggregation ────────────────────────────────────────
    // Single endpoint returns everything the admin dashboard needs.
    // Avoids multiple round trips from the frontend.
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {

        // ── Appointment counts by status ──────────────────────────────
        long totalAppointments    = appointmentRepository.count();
        long confirmedCount       = appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED);
        long pendingCount         = appointmentRepository.countByStatus(AppointmentStatus.PENDING);
        long completedCount       = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
        long cancelledCount       = appointmentRepository.countByStatus(AppointmentStatus.CANCELLED);

        // ── Today's appointments ──────────────────────────────────────
        // Passes current datetime — the repository query extracts just the date part
        List<Long> todaysAppointmentIds = appointmentRepository
            .findByDate(LocalDateTime.now())
            .stream()
            .map(a -> a.getId())
            .toList();

        // ── User counts ───────────────────────────────────────────────
        long totalPatients = patientRepository.findByIsActive(true).size();
        long totalDoctors  = doctorRepository.findByIsActive(true).size();

        // ── Resource occupancy summary ────────────────────────────────
        // Builds a map: { "ICU Beds": 75.0, "Ventilators": 40.0 }
        // Frontend feeds this directly into a Recharts PieChart or BarChart
        Map<String, Double> resourceOccupancy = resourceRepository.findAll()
            .stream()
            .collect(Collectors.toMap(
                r -> r.getName(),
                r -> Math.round(r.getOccupancyRate() * 10.0) / 10.0
                // round to 1 decimal place
            ));

        // ── Available vs Used per resource ───────────────────────────
        // Builds a list of objects for a stacked bar chart:
        // [{ name: "ICU Beds", available: 3, used: 7, total: 10 }, ...]
        List<DashboardStatsResponse.ResourceStat> resourceStats =
            resourceRepository.findAll()
                .stream()
                .map(r -> DashboardStatsResponse.ResourceStat.builder()
                    .name(r.getName())
                    .total(r.getTotalCount())
                    .used(r.getUsedCount())
                    .available(r.getAvailableCount())
                    .occupancyRate(Math.round(r.getOccupancyRate() * 10.0) / 10.0)
                    .build())
                .toList();

        return DashboardStatsResponse.builder()
            .totalAppointments(totalAppointments)
            .confirmedAppointments(confirmedCount)
            .pendingAppointments(pendingCount)
            .completedAppointments(completedCount)
            .cancelledAppointments(cancelledCount)
            .todaysAppointmentCount(todaysAppointmentIds.size())
            .totalActivePatients(totalPatients)
            .totalActiveDoctors(totalDoctors)
            .resourceOccupancy(resourceOccupancy)
            .resourceStats(resourceStats)
            .build();
    }
}