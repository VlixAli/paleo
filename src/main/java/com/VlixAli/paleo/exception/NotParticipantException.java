package com.VlixAli.paleo.exception;

import java.util.UUID;

public class NotParticipantException extends RuntimeException {
    public NotParticipantException(UUID eventId) {
        super("Not a participant of event: " + eventId);
    }
}
