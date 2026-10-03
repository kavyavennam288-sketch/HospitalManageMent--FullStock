package com.medsync.service;

import com.medsync.dto.response.ResourceResponse;
import com.medsync.model.Resource;
import com.medsync.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;

    // ── Get All Resources ─────────────────────────────────────────────
    // Admin dashboard calls this to populate the occupancy chart.
    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources() {
        return resourceRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ── Create a Resource ─────────────────────────────────────────────
    // Admin registers a new resource type for the first time.
    // e.g. "ICU Beds", totalCount=10
    @Transactional
    public ResourceResponse createResource(String name, int totalCount) {
        // Prevent duplicate resource names
        boolean exists = resourceRepository.findAll()
            .stream()
            .anyMatch(r -> r.getName().equalsIgnoreCase(name));

        if (exists) {
            throw new RuntimeException(
                "Resource already exists: " + name
            );
        }

        Resource resource = Resource.builder()
            .name(name)
            .totalCount(totalCount)
            .usedCount(0)   // starts at zero — none in use yet
            .build();

        return mapToResponse(resourceRepository.save(resource));
    }

    // ── Update Used Count ─────────────────────────────────────────────
    // Called when a bed is occupied or freed.
    // usedCount represents current occupancy — admin sets it directly.
    // e.g. "3 ICU beds are now occupied" → usedCount = 3
    @Transactional
    public ResourceResponse updateUsedCount(Long resourceId, int usedCount) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new RuntimeException(
                "Resource not found: " + resourceId
            ));

        // Guard: can't use more than you have
        if (usedCount > resource.getTotalCount()) {
            throw new RuntimeException(
                "Used count (" + usedCount + ") cannot exceed " +
                "total count (" + resource.getTotalCount() + ")"
            );
        }

        if (usedCount < 0) {
            throw new RuntimeException("Used count cannot be negative");
        }

        resource.setUsedCount(usedCount);
        Resource saved = resourceRepository.save(resource);

        log.info("Resource '{}' updated: {}/{} in use",
            saved.getName(), saved.getUsedCount(), saved.getTotalCount());

        return mapToResponse(saved);
    }

    // ── Update Total Count ────────────────────────────────────────────
    // Admin adds more beds / equipment to the hospital.
    @Transactional
    public ResourceResponse updateTotalCount(Long resourceId, int totalCount) {
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new RuntimeException(
                "Resource not found: " + resourceId
            ));

        // Can't reduce total below current usage
        if (totalCount < resource.getUsedCount()) {
            throw new RuntimeException(
                "Total count cannot be less than current used count (" +
                resource.getUsedCount() + ")"
            );
        }

        resource.setTotalCount(totalCount);
        return mapToResponse(resourceRepository.save(resource));
    }

    // ── Delete Resource ───────────────────────────────────────────────
    @Transactional
    public void deleteResource(Long resourceId) {
        if (!resourceRepository.existsById(resourceId)) {
            throw new RuntimeException("Resource not found: " + resourceId);
        }
        resourceRepository.deleteById(resourceId);
        log.info("Resource {} deleted", resourceId);
    }

    // ── Map Entity → Response DTO ─────────────────────────────────────
    private ResourceResponse mapToResponse(Resource resource) {
        return ResourceResponse.builder()
            .id(resource.getId())
            .name(resource.getName())
            .totalCount(resource.getTotalCount())
            .usedCount(resource.getUsedCount())
            // These two are computed from totalCount and usedCount
            // @Transient on the entity — not stored in DB, derived on the fly
            .availableCount(resource.getAvailableCount())
            .occupancyRate(Math.round(resource.getOccupancyRate() * 10.0) / 10.0)
            // Round to 1 decimal: 66.666... → 66.7
            .build();
    }
}