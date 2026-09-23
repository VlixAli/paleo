package com.VlixAli.paleo.service;

import com.VlixAli.paleo.dto.response.EventParticipantResponse;
import com.VlixAli.paleo.entity.Event;
import com.VlixAli.paleo.entity.EventParticipant;
import com.VlixAli.paleo.entity.EventStatus;
import com.VlixAli.paleo.entity.User;
import com.VlixAli.paleo.exception.EventNotFoundException;
import com.VlixAli.paleo.exception.EventNotJoinableException;
import com.VlixAli.paleo.exception.NotParticipantException;
import com.VlixAli.paleo.exception.OwnerCannotLeaveException;
import com.VlixAli.paleo.mapper.EventParticipantMapper;
import com.VlixAli.paleo.repository.EventParticipantRepository;
import com.VlixAli.paleo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventParticipantService {

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final EventParticipantMapper participantMapper;
    private final UserService userService;

    @Transactional
    public EventParticipantResponse join(Authentication authentication, UUID eventId) {
        User user = userService.getOrCreateCurrentUser(authentication);
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw EventNotJoinableException.notPublished(eventId, event.getStatus());
        }
        if (participantRepository.existsByEventIdAndUserId(eventId, user.getId())) {
            throw EventNotJoinableException.alreadyJoined(eventId);
        }
        if (event.getCapacity() != null
                && participantRepository.countByEventId(eventId) >= event.getCapacity()) {
            throw EventNotJoinableException.eventFull(eventId);
        }
        EventParticipant saved = participantRepository.save(EventParticipant.builder()
                .event(event)
                .user(user)
                .build());
        return participantMapper.toResponse(saved);
    }

    @Transactional
    public void leave(Authentication authentication, UUID eventId) {
        User user = userService.getOrCreateCurrentUser(authentication);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        if (event.getOwner().getId().equals(user.getId())) {
            throw new OwnerCannotLeaveException(eventId);
        }
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw EventNotJoinableException.notPublished(eventId, event.getStatus());
        }
        int deleted = participantRepository.deleteByEventIdAndUserId(eventId, user.getId());
        if (deleted == 0) {
            throw new NotParticipantException(eventId);
        }
    }
}
