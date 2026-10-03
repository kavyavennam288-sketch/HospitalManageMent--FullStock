package com.medsync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {
    private Long id;
    private Long doctorId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private boolean available;
    private LocalDateTime slotDateTime;  // exact bookable datetime when date is known
}