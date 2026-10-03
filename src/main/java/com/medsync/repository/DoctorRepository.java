// DoctorRepository.java
package com.medsync.repository;

import com.medsync.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Method name parsing: findBy + Specialty + AndIsActive
    // → WHERE specialty = ? AND is_active = ?
    List<Doctor> findBySpecialtyAndIsActive(String specialty, boolean isActive);

    // @Query lets us write JPQL (object-oriented SQL — uses class names, not table names)
    // This finds doctors who have an available slot on a given day of week
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.availableSlots s " +
           "WHERE s.dayOfWeek = :day AND s.isAvailable = true AND d.isActive = true")
    List<Doctor> findAvailableDoctorsOnDay(@Param("day") DayOfWeek day);

    List<Doctor> findByIsActive(boolean isActive);
}