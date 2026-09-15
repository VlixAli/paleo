package com.VlixAli.paleo.exception;

import com.VlixAli.paleo.entity.EventStatus;

import java.util.UUID;

public class EventNotJoinableException extends RuntimeException {

    public EventNotJoinableException(String message) {
        super(message);
    }

    public static EventNotJoinableException notPublished(UUID eventId, EventStatus status) {
        return new EventNotJoinableException("Event is not joinable (status=" + status + "): " + eventId);
    }

    public static EventNotJoinableException alreadyJoined(UUID eventId) {
        return new EventNotJoinableException("Already joined event: " + eventId);
    }

    public static EventNotJoinableException eventFull(UUID eventId) {
        return new EventNotJoinableException("Event is full: " + eventId);
    }
}
