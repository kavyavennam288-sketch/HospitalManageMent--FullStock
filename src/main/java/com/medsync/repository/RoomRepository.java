// RoomRepository.java
package com.medsync.repository;

import com.medsync.model.Room;
import com.medsync.model.Room.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // List<Room> findByIsAvailableTrue();

    // List<Room> findByTypeAndIsAvailableTrue(RoomType type);
}