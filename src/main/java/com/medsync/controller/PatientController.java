package com.medsync.controller;

import com.medsync.dto.request.UpdatePatientRequest;
import com.medsync.dto.response.PatientResponse;
import com.medsync.service.PatientService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient record management (Admin only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class PatientController {

    private final PatientService patientService;

    // GET /api/patients
    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllActivePatients());
    }

    // GET /api/patients/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    // PUT /api/patients/{id}
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @RequestBody UpdatePatientRequest request) {

        return ResponseEntity.ok(
            patientService.updatePatientInfo(
                id,
                request.getAllergies(),
                request.getEmergencyContact()
            )
        );
    }

    // DELETE /api/patients/{id}
    // Soft delete — sets isActive=false, cancels future appointments
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeletePatient(@PathVariable Long id) {
        patientService.softDeletePatient(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/patients/{id}/reactivate
    // Restore a deactivated patient account
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<PatientResponse> reactivatePatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.reactivatePatient(id));
    }
}