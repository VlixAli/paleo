package com.VlixAli.paleo.dto.response;

import com.VlixAli.paleo.entity.EventStatus;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        String description,
        String city,
        String location,
        Instant startTime,
        Instant endTime,
        Integer capacity,
        UUID owner,
        EventStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
