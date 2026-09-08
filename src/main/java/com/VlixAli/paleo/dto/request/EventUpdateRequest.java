package com.VlixAli.paleo.dto.request;

import com.VlixAli.paleo.annotation.EndTimeAfterStartTime;
import com.VlixAli.paleo.annotation.NullOrNotBlank;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

@EndTimeAfterStartTime
public record EventUpdateRequest(
        @NullOrNotBlank
        String title,
        @NullOrNotBlank
        String description,
        @NullOrNotBlank
        String city,
        @NullOrNotBlank
        String location,
        @Future
        Instant startTime,
        @Future
        Instant endTime,
        @Positive
        Integer capacity
) implements EventTimeRange {
}
