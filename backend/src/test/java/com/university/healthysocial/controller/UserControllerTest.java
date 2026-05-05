package com.university.healthysocial.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.healthysocial.domain.User;
import com.university.healthysocial.dto.request.Requests.UpdateProfileRequest;
import com.university.healthysocial.dto.response.Responses.UserProfileResponse;
import com.university.healthysocial.repository.FollowRepository;
import com.university.healthysocial.repository.UserRepository;
import com.university.healthysocial.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FollowRepository followRepository;

    @MockBean
    private SecurityUtils securityUtils;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .keycloakId("test-keycloak-id")
                .username("testuser")
                .email("test@example.com")
                .bio("Test bio")
                .avatarUrl("http://example.com/avatar.jpg")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    void getMyProfile_ReturnsProfile() throws Exception {
        // Given
        when(securityUtils.getCurrentKeycloakId()).thenReturn("test-keycloak-id");
        when(userRepository.findByKeycloakId("test-keycloak-id")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(followRepository.countByFollowing(testUser)).thenReturn(10L);
        when(followRepository.countByFollower(testUser)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.bio").value("Test bio"))
                .andExpect(jsonPath("$.avatarUrl").value("http://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.followersCount").value(10))
                .andExpect(jsonPath("$.followingCount").value(5));
    }

    @Test
    @WithMockUser
    void updateMyProfile_ValidRequest_UpdatesProfile() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest("newusername", "New bio", "http://example.com/newavatar.jpg");
        when(securityUtils.getCurrentKeycloakId()).thenReturn("test-keycloak-id");
        when(userRepository.findByKeycloakId("test-keycloak-id")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(followRepository.countByFollowing(testUser)).thenReturn(10L);
        when(followRepository.countByFollower(testUser)).thenReturn(5L);

        // When & Then
        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("newusername"))
                .andExpect(jsonPath("$.bio").value("New bio"))
                .andExpect(jsonPath("$.avatarUrl").value("http://example.com/newavatar.jpg"));
    }

    @Test
    @WithMockUser
    void updateMyProfile_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest("", null, null); // Invalid username

        // When & Then
        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getProfile_ExistingUser_ReturnsProfile() throws Exception {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(followRepository.countByFollowing(testUser)).thenReturn(10L);
        when(followRepository.countByFollower(testUser)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/profile", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser
    void getProfile_NonExistingUser_ReturnsNotFound() throws Exception {
        // Given
        UUID nonExistingId = UUID.randomUUID();
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/profile", nonExistingId))
                .andExpect(status().isNotFound());
    }
}
