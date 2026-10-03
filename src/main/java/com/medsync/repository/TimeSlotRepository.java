// TimeSlotRepository.java
package com.medsync.repository;

import com.medsync.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek day);

    List<TimeSlot> findByDoctorIdAndIsAvailableTrue(Long doctorId);
}