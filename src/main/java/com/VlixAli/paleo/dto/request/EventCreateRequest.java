package com.VlixAli.paleo.dto.request;

import com.VlixAli.paleo.annotation.EndTimeAfterStartTime;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

@EndTimeAfterStartTime
public record EventCreateRequest(
        @NotBlank
        String title,
        @NotBlank
        String description,
        @NotBlank
        String city,
        @NotBlank
        String location,
        @NotNull
        @Future
        Instant startTime,
        @Future
        Instant endTime,
        @Positive
        Integer capacity
) implements EventTimeRange {
}
