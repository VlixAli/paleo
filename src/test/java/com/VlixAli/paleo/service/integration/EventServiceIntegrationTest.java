package com.VlixAli.paleo.service.integration;

import com.VlixAli.paleo.entity.Event;
import com.VlixAli.paleo.entity.EventStatus;
import com.VlixAli.paleo.entity.User;
import com.VlixAli.paleo.dto.request.EventUpdateRequest;
import com.VlixAli.paleo.exception.EventNotFoundException;
import com.VlixAli.paleo.mapper.EventMapperImpl;
import com.VlixAli.paleo.mapper.UserMapperImpl;
import com.VlixAli.paleo.repository.EventRepository;
import com.VlixAli.paleo.repository.UserRepository;
import com.VlixAli.paleo.service.EventService;
import com.VlixAli.paleo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@Import({EventService.class, UserService.class, EventMapperImpl.class, UserMapperImpl.class})
class EventServiceIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    @Test
    void deleteHidesEventFromFindListAndGet() {
        var owner = user("kc-alice", "alice", "Alice");
        var event = eventRepository.save(eventOwnedBy(owner, EventStatus.PUBLISHED));

        eventService.delete(auth("kc-alice", "alice", "Alice"), event.getId());

        assertThat(eventRepository.findById(event.getId())).isEmpty();
        assertThat(eventService.list()).isEmpty();
        assertThatThrownBy(() -> eventService.getById(event.getId()))
                .isInstanceOf(EventNotFoundException.class);
        assertThatThrownBy(() -> eventService.delete(auth("kc-alice", "alice", "Alice"), event.getId()))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void nonOwnerCannotDelete() {
        var owner = user("kc-alice", "alice", "Alice");
        var event = eventRepository.save(eventOwnedBy(owner, EventStatus.DRAFT));

        assertThatThrownBy(() -> eventService.delete(auth("kc-bob", "bob", "Bob"), event.getId()))
                .isInstanceOf(EventNotFoundException.class);
        assertThat(eventRepository.findById(event.getId())).isPresent();
    }

    @Test
    void updatePersistsWithoutExplicitSave() {
        var owner = user("kc-alice", "alice", "Alice");
        var event = eventRepository.save(eventOwnedBy(owner, EventStatus.DRAFT));

        var response = eventService.update(auth("kc-alice", "alice", "Alice"), event.getId(),
                new EventUpdateRequest("new-t", null, null, null, null, null, null));

        assertThat(response.title()).isEqualTo("new-t");
        var reloaded = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("new-t");
        assertThat(reloaded.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(reloaded.getUpdatedAt()).isNotNull();
        assertThat(!reloaded.getUpdatedAt().isBefore(reloaded.getCreatedAt())).isTrue();
    }

    private User user(String keycloakUserId, String username, String displayName) {
        return userRepository.save(User.builder()
                .keycloakUserId(keycloakUserId).username(username).displayName(displayName).build());
    }

    private Event eventOwnedBy(User owner, EventStatus status) {
        return Event.builder().title("t").description("d").city("c").location("l")
                .startTime(Instant.parse("2030-01-01T10:00:00Z"))
                .endTime(Instant.parse("2030-01-01T12:00:00Z"))
                .owner(owner).status(status).build();
    }

    private static JwtAuthenticationToken auth(String subject, String username, String name) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claim("preferred_username", username);
        if (name != null) {
            builder.claim("name", name);
        }
        return new JwtAuthenticationToken(builder.build());
    }
}
