package com.VlixAli.paleo.controller.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// ponytail: H2 props inline — same Liquibase changelog as the @DataJpaTest slices;
// extract to application-test.yml only if a second full-stack IT needs it.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:eventcontrollertest;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureMockMvc
@Transactional
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createThenGet() throws Exception {
        String id = createEvent("kc-alice", "alice");

        mockMvc.perform(get("/api/events/{id}", id).with(jwt("kc-alice", "alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("t"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void publishAsOwner() throws Exception {
        String id = createEvent("kc-alice", "alice");

        mockMvc.perform(post("/api/events/{id}/publish", id)
                        .with(jwt("kc-alice", "alice"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void nonOwnerPublishReturns404() throws Exception {
        String id = createEvent("kc-alice", "alice");

        mockMvc.perform(post("/api/events/{id}/publish", id)
                        .with(jwt("kc-bob", "bob"))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/events/{id}", id).with(jwt("kc-alice", "alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void deleteThenGet404() throws Exception {
        String id = createEvent("kc-alice", "alice");

        mockMvc.perform(delete("/api/events/{id}", id)
                        .with(jwt("kc-alice", "alice"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/events/{id}", id).with(jwt("kc-alice", "alice")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createInvalidReturns400() throws Exception {
        mockMvc.perform(post("/api/events")
                        .with(jwt("kc-alice", "alice"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson().replace("\"title\":\"t\"", "\"title\":\"\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patchAsOwnerPersists() throws Exception {
        String id = createEvent("kc-alice", "alice");

        mockMvc.perform(patch("/api/events/{id}", id)
                        .with(jwt("kc-alice", "alice"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"new\",\"capacity\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("new"))
                .andExpect(jsonPath("$.capacity").value(20))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.updatedAt").exists());

        mockMvc.perform(get("/api/events/{id}", id).with(jwt("kc-alice", "alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("new"))
                .andExpect(jsonPath("$.capacity").value(20));
    }

    @Test
    void nonOwnerPatchReturns404AndUnchanged() throws Exception {
        String id = createEvent("kc-alice", "alice");

        mockMvc.perform(patch("/api/events/{id}", id)
                        .with(jwt("kc-bob", "bob"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"hijacked\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/events/{id}", id).with(jwt("kc-alice", "alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("t"));
    }

    @Test
    void patchStartPastStoredEndReturns400() throws Exception {
        String id = createEvent("kc-alice", "alice");

        mockMvc.perform(patch("/api/events/{id}", id)
                        .with(jwt("kc-alice", "alice"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startTime\":\"2030-01-01T13:00:00Z\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/events/{id}", id).with(jwt("kc-alice", "alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("2030-01-01T10:00:00Z"));
    }

    @Test
    void patchBlankTitleReturns400() throws Exception {
        String id = createEvent("kc-alice", "alice");

        mockMvc.perform(patch("/api/events/{id}", id)
                        .with(jwt("kc-alice", "alice"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedPatchReturns401() throws Exception {
        mockMvc.perform(patch("/api/events/{id}", UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"new\"}"))
                .andExpect(status().isUnauthorized());
    }

    private String createEvent(String subject, String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/events")
                        .with(jwt(subject, username))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("id").asString()).isNotBlank();
        return body.get("id").asString();
    }

    private static String validCreateJson() {
        return """
                {"title":"t","description":"d","city":"c","location":"l",\
                "startTime":"2030-01-01T10:00:00Z","endTime":"2030-01-01T12:00:00Z","capacity":10}\
                """;
    }

    private static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwt(
            String subject, String username) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(builder -> builder
                        .subject(subject)
                        .claim("preferred_username", username));
    }
}
