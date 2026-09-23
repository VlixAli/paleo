package com.VlixAli.paleo.service;

import com.VlixAli.paleo.dto.request.EventCreateRequest;
import com.VlixAli.paleo.dto.request.EventUpdateRequest;
import com.VlixAli.paleo.dto.response.EventResponse;
import com.VlixAli.paleo.entity.Event;
import com.VlixAli.paleo.entity.EventStatus;
import com.VlixAli.paleo.entity.User;
import com.VlixAli.paleo.entity.EventParticipant;
import com.VlixAli.paleo.exception.EventNotFoundException;
import com.VlixAli.paleo.mapper.EventMapper;
import com.VlixAli.paleo.repository.EventParticipantRepository;
import com.VlixAli.paleo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final EventMapper eventMapper;
    private final UserService userService;

    @Transactional
    public EventResponse create(Authentication authentication, EventCreateRequest request) {
        User creator = userService.getOrCreateCurrentUser(authentication);
        return eventMapper.eventToEventResponse(eventRepository.save(eventMapper.toEntity(request, creator)));
    }

    @Transactional
    public EventResponse publish(Authentication authentication, UUID id) {
        return changeStatus(authentication, id, EventStatus.PUBLISHED);
    }

    @Transactional
    public EventResponse cancel(Authentication authentication, UUID id) {
        return changeStatus(authentication, id, EventStatus.CANCELLED);
    }

    @Transactional
    public EventResponse draft(Authentication authentication, UUID id) {
        return changeStatus(authentication, id, EventStatus.DRAFT);
    }

    @Transactional
    public void delete(Authentication authentication, UUID id) {
        eventRepository.delete(getOwnedEvent(authentication, id));
    }

    @Transactional
    public EventResponse update(Authentication authentication, UUID id, EventUpdateRequest request) {
        Event event = getOwnedEvent(authentication, id);
        Instant start = request.startTime() != null ? request.startTime() : event.getStartTime();
        Instant end = request.endTime() != null ? request.endTime() : event.getEndTime();
        validateTimeRange(start, end);
        eventMapper.updateEventFromRequest(request, event);
        return eventMapper.eventToEventResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> list() {
        return eventRepository.findAll().stream()
                .map(eventMapper::eventToEventResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getById(UUID id) {
        return eventRepository.findById(id)
                .map(eventMapper::eventToEventResponse)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    private Event getOwnedEvent(Authentication authentication, UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
        User current = userService.getOrCreateCurrentUser(authentication);
        if (!event.getOwner().getId().equals(current.getId())) {
            throw new EventNotFoundException(id);
        }
        return event;
    }

    private void validateTimeRange(Instant startTime, Instant endTime) {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    private EventResponse changeStatus(Authentication authentication, UUID id, EventStatus status) {
        Event event = getOwnedEvent(authentication, id);
        event.setStatus(status);
        if (status == EventStatus.PUBLISHED) {
            ensureOwnerParticipant(event);
        }
        return eventMapper.eventToEventResponse(event);
    }

    private void ensureOwnerParticipant(Event event) {
        UUID eventId = event.getId();
        User owner = event.getOwner();
        if (participantRepository.existsByEventIdAndUserId(eventId, owner.getId())) {
            return;
        }
        try {
            participantRepository.save(EventParticipant.builder()
                    .event(event)
                    .user(owner)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // ponytail: concurrent publish race; unique constraint is the backstop
        }
    }
}
