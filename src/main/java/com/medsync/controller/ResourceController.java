package com.medsync.controller;

import com.medsync.dto.request.UpdateResourceRequest;
import com.medsync.dto.response.ResourceResponse;
import com.medsync.service.ResourceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Tag(name = "Resources", description = "Hospital resource and bed management (Admin only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
// Class-level @PreAuthorize applies to ALL methods.
// No individual method needs it — cleaner than repeating it everywhere.
public class ResourceController {

    private final ResourceService resourceService;

    // GET /api/resources
    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    // POST /api/resources?name=ICU Beds&totalCount=10
    @PostMapping
    public ResponseEntity<ResourceResponse> createResource(
            @RequestParam String name,
            @RequestParam int totalCount) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(resourceService.createResource(name, totalCount));
    }

    // PUT /api/resources/{id}
    // Admin updates either usedCount or totalCount (or both)
    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody UpdateResourceRequest request) {

        ResourceResponse response = null;

        // Update whichever counts were provided in the request body
        if (request.getUsedCount() != null) {
            response = resourceService.updateUsedCount(id, request.getUsedCount());
        }
        if (request.getTotalCount() != null) {
            response = resourceService.updateTotalCount(id, request.getTotalCount());
        }

        if (response == null) {
            throw new RuntimeException("Provide at least usedCount or totalCount to update");
        }

        return ResponseEntity.ok(response);
    }

    // DELETE /api/resources/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        // 204 No Content = success with no body (standard for DELETE)
        return ResponseEntity.noContent().build();
    }
}