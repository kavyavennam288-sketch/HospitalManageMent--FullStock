package com.medsync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {
    private Long id;
    private String name;
    private int totalCount;
    private int usedCount;
    private int availableCount;     // totalCount - usedCount
    private double occupancyRate;   // usedCount / totalCount * 100, rounded to 1dp
}