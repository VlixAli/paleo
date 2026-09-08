package com.VlixAli.paleo.service.unit;

import com.VlixAli.paleo.dto.request.UserUpdateRequest;
import com.VlixAli.paleo.dto.response.UserResponse;
import com.VlixAli.paleo.entity.User;
import com.VlixAli.paleo.mapper.UserMapperImpl;
import com.VlixAli.paleo.repository.UserRepository;
import com.VlixAli.paleo.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapperImpl userMapper = new UserMapperImpl();

    @InjectMocks
    private UserService userService;

    @Test
    void existingUserReturnsExistingWithoutSave() {
        User existing = User.builder()
                .keycloakUserId("kc-alice").username("alice").displayName("Alice").build();
        when(userRepository.findByKeycloakUserId("kc-alice")).thenReturn(Optional.of(existing));

        UserResponse response = userService.me(auth("kc-alice", "alice", "Alice"));

        assertThat(response.username()).isEqualTo("alice");
        verify(userRepository, never()).save(any());
    }

    @Test
    void unknownKeycloakIdCreatesUser() {
        when(userRepository.findByKeycloakUserId("kc-new")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.me(auth("kc-new", "newbie", null));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getKeycloakUserId()).isEqualTo("kc-new");
        assertThat(saved.getValue().getUsername()).isEqualTo("newbie");
        assertThat(saved.getValue().getDisplayName()).isEqualTo("newbie");
        assertThat(response.username()).isEqualTo("newbie");
    }

    @Test
    void updateMeAppliesOnlyProvidedFields() {
        User existing = User.builder()
                .keycloakUserId("kc-alice").username("alice").displayName("Alice").bio("old").build();
        when(userRepository.findByKeycloakUserId("kc-alice")).thenReturn(Optional.of(existing));

        UserResponse response = userService.updateMe(
                auth("kc-alice", "alice", "Alice"),
                new UserUpdateRequest(null, "Alice A.", null, null, "new bio"));

        assertThat(response.displayName()).isEqualTo("Alice A.");
        assertThat(response.bio()).isEqualTo("new bio");
        assertThat(response.username()).isEqualTo("alice");
        verify(userRepository, never()).existsByUsername(any());
    }

    @Test
    void updateMeAppliesCities() {
        User existing = User.builder()
                .keycloakUserId("kc-alice").username("alice").displayName("Alice").build();
        when(userRepository.findByKeycloakUserId("kc-alice")).thenReturn(Optional.of(existing));

        UserResponse response = userService.updateMe(
                auth("kc-alice", "alice", "Alice"),
                new UserUpdateRequest(null, null, "Berlin", "Munich", null));

        assertThat(response.homeCity()).isEqualTo("Berlin");
        assertThat(response.workCity()).isEqualTo("Munich");
        assertThat(response.username()).isEqualTo("alice");
    }

    @Test
    void updateMeTakenUsernameConflicts() {
        User existing = User.builder()
                .keycloakUserId("kc-alice").username("alice").displayName("Alice").build();
        when(userRepository.findByKeycloakUserId("kc-alice")).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateMe(
                auth("kc-alice", "alice", "Alice"),
                new UserUpdateRequest("bob", null, null, null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
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
