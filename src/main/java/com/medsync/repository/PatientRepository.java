// PatientRepository.java
package com.medsync.repository;

import com.medsync.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Soft-delete support: isActive = false hides the patient from lists
    // but keeps their record in the DB (medical records can't be hard-deleted)
    List<Patient> findByIsActive(boolean isActive);
}   