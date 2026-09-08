package com.VlixAli.paleo.controller.unit;

import com.VlixAli.paleo.controller.EventController;
import com.VlixAli.paleo.dto.request.EventCreateRequest;
import com.VlixAli.paleo.dto.response.EventResponse;
import com.VlixAli.paleo.entity.EventStatus;
import com.VlixAli.paleo.exception.EventNotFoundException;
import com.VlixAli.paleo.security.SecurityConfig;
import com.VlixAli.paleo.service.EventService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(SecurityConfig.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void unauthenticatedGetReturns401() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedCreateReturns401() throws Exception {
        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReturns201AndDelegates() throws Exception {
        when(eventService.create(any(), any())).thenReturn(response(EventStatus.DRAFT));

        mockMvc.perform(post("/api/events")
                        .with(jwt("kc-alice", "alice"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("t"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        ArgumentCaptor<Authentication> auth = ArgumentCaptor.forClass(Authentication.class);
        ArgumentCaptor<EventCreateRequest> request = ArgumentCaptor.forClass(EventCreateRequest.class);
        verify(eventService).create(auth.capture(), request.capture());
        assertThat(((JwtAuthenticationToken) auth.getValue()).getToken().getSubject())
                .isEqualTo("kc-alice");
        assertThat(request.getValue().title()).isEqualTo("t");
    }

    @Test
    void createInvalidReturns400() throws Exception {
        mockMvc.perform(post("/api/events")
                        .with(jwt("kc-alice", "alice"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson().replace("\"title\":\"t\"", "\"title\":\"  \"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdReturns200() throws Exception {
        var id = UUID.randomUUID();
        when(eventService.getById(id)).thenReturn(response(EventStatus.PUBLISHED));

        mockMvc.perform(get("/api/events/{id}", id).with(jwt("kc-alice", "alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void getByIdMissingReturns404() throws Exception {
        var id = UUID.randomUUID();
        when(eventService.getById(id)).thenThrow(new EventNotFoundException(id));

        mockMvc.perform(get("/api/events/{id}", id).with(jwt("kc-alice", "alice")))
                .andExpect(status().isNotFound());
    }

    @Test
    void publishDelegatesToService() throws Exception {
        var id = UUID.randomUUID();
        when(eventService.publish(any(), any())).thenReturn(response(EventStatus.PUBLISHED));

        mockMvc.perform(post("/api/events/{id}/publish", id)
                        .with(jwt("kc-alice", "alice"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(eventService).publish(any(Authentication.class), any(UUID.class));
    }

    @Test
    void deleteReturns204() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(delete("/api/events/{id}", id)
                        .with(jwt("kc-alice", "alice"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(eventService).delete(any(Authentication.class), any(UUID.class));
    }

    private static String validCreateJson() {
        return """
                {"title":"t","description":"d","city":"c","location":"l",\
                "startTime":"2030-01-01T10:00:00Z","endTime":"2030-01-01T12:00:00Z","capacity":10}\
                """;
    }

    private static EventResponse response(EventStatus status) {
        return new EventResponse(UUID.randomUUID(), "t", "d", "c", "l",
                Instant.parse("2030-01-01T10:00:00Z"), Instant.parse("2030-01-01T12:00:00Z"),
                10, UUID.randomUUID(), status, Instant.now(), Instant.now());
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwt(
            String subject, String username) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(builder -> builder
                        .subject(subject)
                        .claim("preferred_username", username));
    }
}
