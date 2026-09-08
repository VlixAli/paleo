package com.VlixAli.paleo.service.unit;

import com.VlixAli.paleo.dto.request.EventCreateRequest;
import com.VlixAli.paleo.dto.request.EventUpdateRequest;
import com.VlixAli.paleo.entity.Event;
import com.VlixAli.paleo.entity.EventStatus;
import com.VlixAli.paleo.entity.User;
import com.VlixAli.paleo.exception.EventNotFoundException;
import com.VlixAli.paleo.mapper.EventMapperImpl;
import com.VlixAli.paleo.repository.EventRepository;
import com.VlixAli.paleo.service.EventService;
import com.VlixAli.paleo.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Spy
    private EventMapperImpl eventMapper = new EventMapperImpl();

    @Mock
    private UserService userService;

    @InjectMocks
    private EventService eventService;

    @Test
    void createDefaultsToDraft() {
        var owner = userWithId(UUID.randomUUID());
        when(userService.getOrCreateCurrentUser(null)).thenReturn(owner);
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        var request = new EventCreateRequest("t", null, null, null,
                Instant.parse("2030-01-01T10:00:00Z"), Instant.parse("2030-01-01T12:00:00Z"), null);
        var response = eventService.create(null, request);

        var saved = ArgumentCaptor.forClass(Event.class);
        org.mockito.Mockito.verify(eventRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(saved.getValue().getOwner()).isEqualTo(owner);
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void publishFromCancelled() {
        var owner = userWithId(UUID.randomUUID());
        when(userService.getOrCreateCurrentUser(null)).thenReturn(owner);
        when(eventRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(eventOwnedBy(owner, EventStatus.CANCELLED)));

        assertThat(eventService.publish(null, UUID.randomUUID()).status()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void cancelSetsCancelled() {
        var owner = userWithId(UUID.randomUUID());
        when(userService.getOrCreateCurrentUser(null)).thenReturn(owner);
        when(eventRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(eventOwnedBy(owner, EventStatus.PUBLISHED)));

        assertThat(eventService.cancel(null, UUID.randomUUID()).status()).isEqualTo(EventStatus.CANCELLED);
    }

    @Test
    void draftFromCancelled() {
        var owner = userWithId(UUID.randomUUID());
        when(userService.getOrCreateCurrentUser(null)).thenReturn(owner);
        when(eventRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(eventOwnedBy(owner, EventStatus.CANCELLED)));

        assertThat(eventService.draft(null, UUID.randomUUID()).status()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void nonOwnerCannotChangeStatus() {
        when(userService.getOrCreateCurrentUser(null)).thenReturn(userWithId(UUID.randomUUID()));
        when(eventRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(eventOwnedBy(userWithId(UUID.randomUUID()), EventStatus.DRAFT)));

        assertThatThrownBy(() -> eventService.publish(null, UUID.randomUUID()))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void updateAppliesOnlyNonNullFields() {
        var owner = userWithId(UUID.randomUUID());
        var event = eventOwnedBy(owner, EventStatus.DRAFT);
        event.setDescription("orig-d");
        event.setCity("orig-c");
        when(userService.getOrCreateCurrentUser(null)).thenReturn(owner);
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(event));

        var response = eventService.update(null, event.getId(),
                new EventUpdateRequest("new-t", null, null, null, null, null, null));

        assertThat(response.title()).isEqualTo("new-t");
        assertThat(response.description()).isEqualTo("orig-d");
        assertThat(response.city()).isEqualTo("orig-c");
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void updateKeepsStatusAndOwnership() {
        var owner = userWithId(UUID.randomUUID());
        var event = eventOwnedBy(owner, EventStatus.PUBLISHED);
        when(userService.getOrCreateCurrentUser(null)).thenReturn(owner);
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(event));

        var response = eventService.update(null, event.getId(),
                new EventUpdateRequest("t2", "d2", "c2", "l2",
                        Instant.parse("2030-02-01T10:00:00Z"), Instant.parse("2030-02-01T12:00:00Z"), 5));

        assertThat(response.status()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(response.owner()).isEqualTo(owner.getId());
        assertThat(response.title()).isEqualTo("t2");
        assertThat(response.capacity()).isEqualTo(5);
    }

    @Test
    void updateStartPastStoredEndFails() {
        var owner = userWithId(UUID.randomUUID());
        when(userService.getOrCreateCurrentUser(null)).thenReturn(owner);
        when(eventRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(eventOwnedBy(owner, EventStatus.DRAFT)));

        var request = new EventUpdateRequest(null, null, null, null,
                Instant.parse("2030-01-01T13:00:00Z"), null, null);
        assertThatThrownBy(() -> eventService.update(null, UUID.randomUUID(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endTime must be after startTime");
    }

    @Test
    void updateEndBeforeStoredStartFails() {
        var owner = userWithId(UUID.randomUUID());
        when(userService.getOrCreateCurrentUser(null)).thenReturn(owner);
        when(eventRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(eventOwnedBy(owner, EventStatus.DRAFT)));

        var request = new EventUpdateRequest(null, null, null, null,
                null, Instant.parse("2030-01-01T09:00:00Z"), null);
        assertThatThrownBy(() -> eventService.update(null, UUID.randomUUID(), request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nonOwnerCannotUpdate() {
        when(userService.getOrCreateCurrentUser(null)).thenReturn(userWithId(UUID.randomUUID()));
        when(eventRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(eventOwnedBy(userWithId(UUID.randomUUID()), EventStatus.DRAFT)));

        var request = new EventUpdateRequest("x", null, null, null, null, null, null);
        assertThatThrownBy(() -> eventService.update(null, UUID.randomUUID(), request))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void updateMissingEventThrowsNotFound() {
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        var request = new EventUpdateRequest("x", null, null, null, null, null, null);
        assertThatThrownBy(() -> eventService.update(null, UUID.randomUUID(), request))
                .isInstanceOf(EventNotFoundException.class);
    }

    private static User userWithId(UUID id) {
        return User.builder().id(id).keycloakUserId("kc-" + id).username("u-" + id).displayName("d").build();
    }

    private static Event eventOwnedBy(User owner, EventStatus status) {
        return Event.builder().id(UUID.randomUUID()).title("t")
                .startTime(Instant.parse("2030-01-01T10:00:00Z"))
                .endTime(Instant.parse("2030-01-01T12:00:00Z"))
                .owner(owner).status(status).build();
    }
}
