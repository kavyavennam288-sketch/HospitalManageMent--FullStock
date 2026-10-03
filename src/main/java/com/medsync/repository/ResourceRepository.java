// ResourceRepository.java
package com.medsync.repository;

import com.medsync.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    // JpaRepository gives us everything we need here — no custom methods required
}