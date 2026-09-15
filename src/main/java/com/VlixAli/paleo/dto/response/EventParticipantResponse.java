package com.VlixAli.paleo.dto.response;

import java.time.Instant;
import java.util.UUID;

public record EventParticipantResponse(
        UUID id,
        UUID eventId,
        UUID userId,
        Instant joinedAt
) {
}
