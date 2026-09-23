package com.VlixAli.paleo.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ParticipantResponse(
        UUID userId,
        String displayName,
        Instant joinedAt
) {
}
