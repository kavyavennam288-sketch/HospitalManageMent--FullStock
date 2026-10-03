// UpdatePatientRequest.java
package com.medsync.dto.request;

import lombok.Data;

@Data
public class UpdatePatientRequest {
    private String allergies;
    private String emergencyContact;
    // Nullable fields — only provided fields are updated in PatientService
}