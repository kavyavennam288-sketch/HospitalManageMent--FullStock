// UpdateResourceRequest.java
package com.medsync.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateResourceRequest {

    private String name;

    @Min(value = 0, message = "Count cannot be negative")
    private Integer totalCount;   // Integer (not int) so null = "not provided"

    @Min(value = 0, message = "Count cannot be negative")
    private Integer usedCount;
}