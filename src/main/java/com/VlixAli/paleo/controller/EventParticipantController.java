package com.VlixAli.paleo.controller;

import com.VlixAli.paleo.dto.response.EventParticipantResponse;
import com.VlixAli.paleo.dto.response.ParticipantResponse;
import com.VlixAli.paleo.service.EventParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventParticipantController {

    private final EventParticipantService eventParticipantService;

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.CREATED)
    public EventParticipantResponse join(Authentication authentication, @PathVariable UUID id) {
        return eventParticipantService.join(authentication, id);
    }

    @PostMapping("/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(Authentication authentication, @PathVariable UUID id) {
        eventParticipantService.leave(authentication, id);
    }

    @GetMapping("/{id}/participants")
    public Page<ParticipantResponse> list(@PathVariable UUID id, Pageable pageable) {
        return eventParticipantService.getEventParticipants(id, pageable);
    }
}
