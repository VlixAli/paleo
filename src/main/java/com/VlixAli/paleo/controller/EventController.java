package com.VlixAli.paleo.controller;

import com.VlixAli.paleo.dto.request.EventCreateRequest;
import com.VlixAli.paleo.dto.request.EventUpdateRequest;
import com.VlixAli.paleo.dto.response.EventResponse;
import com.VlixAli.paleo.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(Authentication authentication, @RequestBody @Valid EventCreateRequest request) {
        return eventService.create(authentication, request);
    }

    @GetMapping
    public List<EventResponse> list() {
        return eventService.list();
    }

    @GetMapping("/{id}")
    public EventResponse getById(@PathVariable UUID id) {
        return eventService.getById(id);
    }

    @PostMapping("/{id}/publish")
    public EventResponse publish(Authentication authentication, @PathVariable UUID id) {
        return eventService.publish(authentication, id);
    }

    @PostMapping("/{id}/cancel")
    public EventResponse cancel(Authentication authentication, @PathVariable UUID id) {
        return eventService.cancel(authentication, id);
    }

    @PostMapping("/{id}/draft")
    public EventResponse draft(Authentication authentication, @PathVariable UUID id) {
        return eventService.draft(authentication, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable UUID id) {
        eventService.delete(authentication, id);
    }

    @PatchMapping("/{id}")
    public EventResponse update(Authentication authentication, @PathVariable UUID id,
                                @RequestBody @Valid EventUpdateRequest request) {
        return eventService.update(authentication, id, request);
    }
}
