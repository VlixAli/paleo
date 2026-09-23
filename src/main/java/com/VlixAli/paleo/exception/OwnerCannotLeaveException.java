package com.VlixAli.paleo.exception;

import java.util.UUID;

public class OwnerCannotLeaveException extends RuntimeException {
    public OwnerCannotLeaveException(UUID eventId) {
        super("Owner cannot leave event: " + eventId);
    }
}
