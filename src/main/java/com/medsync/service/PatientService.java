package com.medsync.service;

import com.medsync.dto.response.PatientResponse;
import com.medsync.model.Patient;
import com.medsync.repository.AppointmentRepository;
import com.medsync.repository.PatientRepository;
import com.medsync.repository.ResourceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    // ADD this field to PatientService:
private final ResourceRepository resourceRepository;
    // ── Get All Active Patients ───────────────────────────────────────
    // Admin sees all patients who haven't been soft-deleted
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllActivePatients() {
        return patientRepository.findByIsActive(true)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ── Get Patient by ID ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException(
                "Patient not found: " + patientId
            ));
        return mapToResponse(patient);
    }

    // ── Soft Delete ───────────────────────────────────────────────────
    // Sets isActive = false instead of deleting the row.
    // The patient record stays in the DB for medical/legal audit purposes.
    // Their future appointments are also cancelled automatically.
    @Transactional
    public void softDeletePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException(
                "Patient not found: " + patientId
            ));

        if (!patient.isActive()) {
            throw new RuntimeException("Patient is already deactivated");
        }

        // Deactivate the patient account
        patient.setActive(false);
        patientRepository.save(patient);

        // Cancel all their upcoming (non-completed) appointments
        // We don't want doctors waiting for patients who no longer have accounts
        appointmentRepository.findByPatientId(patientId)
            .stream()
            .filter(a -> a.getStatus() != com.medsync.model.Appointment.AppointmentStatus.COMPLETED)
            .filter(a -> a.getStatus() != com.medsync.model.Appointment.AppointmentStatus.CANCELLED)
            .forEach(a -> {
    a.setStatus(com.medsync.model.Appointment.AppointmentStatus.CANCELLED);
    a.setCancellationReason("Patient account deactivated by admin");
    appointmentRepository.save(a);
    // Free up the consultation room resource for each cancelled appointment
    resourceRepository.findAll().stream()
        .filter(r -> r.getName().equalsIgnoreCase("Consultation Rooms"))
        .findFirst()
        .ifPresent(r -> {
            if (r.getUsedCount() > 0) {
                r.setUsedCount(r.getUsedCount() - 1);
                resourceRepository.save(r);
            }
        });
});

        log.info("Patient {} soft-deleted. Related appointments cancelled.", patientId);
    }

    // ── Reactivate Patient ────────────────────────────────────────────
    // Admin can restore a deactivated patient account.
    @Transactional
    public PatientResponse reactivatePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException(
                "Patient not found: " + patientId
            ));

        if (patient.isActive()) {
            throw new RuntimeException("Patient is already active");
        }

        patient.setActive(true);
        Patient saved = patientRepository.save(patient);

        log.info("Patient {} reactivated", patientId);
        return mapToResponse(saved);
    }

    // ── Update Patient Notes ──────────────────────────────────────────
    // Admin or doctor updates patient allergies / emergency contact
    @Transactional
    public PatientResponse updatePatientInfo(
            Long patientId,
            String allergies,
            String emergencyContact) {

        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException(
                "Patient not found: " + patientId
            ));

        // Only update fields that were actually provided
        // null check = "don't overwrite if not sent"
        if (allergies != null) patient.setAllergies(allergies);
        if (emergencyContact != null) patient.setEmergencyContact(emergencyContact);

        return mapToResponse(patientRepository.save(patient));
    }

    // ── Map Entity → Response DTO ─────────────────────────────────────
    private PatientResponse mapToResponse(Patient patient) {
        return PatientResponse.builder()
            .id(patient.getId())
            .fullName(patient.getFullName())
            .email(patient.getEmail())
            .dateOfBirth(patient.getDateOfBirth())
            .bloodGroup(patient.getBloodGroup() != null
                ? patient.getBloodGroup().name()
                : null)
            .allergies(patient.getAllergies())
            .emergencyContact(patient.getEmergencyContact())
            .active(patient.isActive())
            .createdAt(patient.getCreatedAt())
            .build();
    }
}