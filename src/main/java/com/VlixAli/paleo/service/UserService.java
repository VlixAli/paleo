package com.VlixAli.paleo.service;

import com.VlixAli.paleo.dto.request.UserUpdateRequest;
import com.VlixAli.paleo.dto.response.UserResponse;
import com.VlixAli.paleo.entity.User;
import com.VlixAli.paleo.mapper.UserMapper;
import com.VlixAli.paleo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse me(Authentication authentication) {
        return userMapper.userToUserResponse(getOrCreateCurrentUser(authentication));
    }

    @Transactional
    public UserResponse updateMe(Authentication authentication, UserUpdateRequest request) {
        User user = getOrCreateCurrentUser(authentication);
        if (request.username() != null && !request.username().equals(user.getUsername())
                && userRepository.existsByUsername(request.username())) {
            // ponytail: check-then-act race, DB unique constraint is the backstop; catch DataIntegrityViolationException when 409s matter
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username is already taken");
        }
        userMapper.updateUserFromRequest(request, user);
        return userMapper.userToUserResponse(user);
    }

    @Transactional
    public User getOrCreateCurrentUser(Authentication authentication) {
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();

        String keycloakUserId = jwt.getSubject();
        if (keycloakUserId == null || keycloakUserId.isBlank()) {
            throw new IllegalArgumentException("JWT subject (sub) is missing");
        }

        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("JWT claim 'preferred_username' is missing");
        }

        String nameClaim = jwt.getClaimAsString("name");
        String displayName = (nameClaim == null || nameClaim.isBlank()) ? username : nameClaim;

        return userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .keycloakUserId(keycloakUserId)
                        .username(username)
                        .displayName(displayName)
                        .build()));
    }
}
