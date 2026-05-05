package com.university.healthysocial.service;

import com.university.healthysocial.domain.User;
import com.university.healthysocial.dto.request.Requests.UpdateProfileRequest;
import com.university.healthysocial.dto.response.Responses.UserProfileResponse;
import com.university.healthysocial.exception.ResourceNotFoundException;
import com.university.healthysocial.mapper.UserMapper;
import com.university.healthysocial.repository.FollowRepository;
import com.university.healthysocial.repository.UserRepository;
import com.university.healthysocial.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserService userService;

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
    void resolveCurrentUser_ExistingUser_ReturnsUser() {
        // Given
        when(securityUtils.getCurrentKeycloakId()).thenReturn("test-keycloak-id");
        when(userRepository.findByKeycloakId("test-keycloak-id")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.resolveCurrentUser();

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resolveCurrentUser_NewUser_CreatesAndReturnsUser() {
        // Given
        when(securityUtils.getCurrentKeycloakId()).thenReturn("new-keycloak-id");
        when(securityUtils.getCurrentPreferredUsername()).thenReturn(Optional.of("newuser"));
        when(securityUtils.getCurrentEmail()).thenReturn(Optional.of("new@example.com"));
        when(userRepository.findByKeycloakId("new-keycloak-id")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // When
        User result = userService.resolveCurrentUser();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getKeycloakId()).isEqualTo("new-keycloak-id");
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getMyProfile_ReturnsProfile() {
        // Given
        when(securityUtils.getCurrentKeycloakId()).thenReturn("test-keycloak-id");
        when(userRepository.findByKeycloakId("test-keycloak-id")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(followRepository.countByFollowing(testUser)).thenReturn(10L);
        when(followRepository.countByFollower(testUser)).thenReturn(5L);
        UserProfileResponse expectedResponse = new UserProfileResponse(
                userId, "testuser", "test@example.com", "Test bio",
                "http://example.com/avatar.jpg", 10L, 5L, testUser.getCreatedAt()
        );
        when(userMapper.toProfileResponse(testUser, 10L, 5L)).thenReturn(expectedResponse);

        // When
        UserProfileResponse result = userService.getMyProfile();

        // Then
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getProfile_ExistingUser_ReturnsProfile() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(followRepository.countByFollowing(testUser)).thenReturn(10L);
        when(followRepository.countByFollower(testUser)).thenReturn(5L);
        UserProfileResponse expectedResponse = new UserProfileResponse(
                userId, "testuser", "test@example.com", "Test bio",
                "http://example.com/avatar.jpg", 10L, 5L, testUser.getCreatedAt()
        );
        when(userMapper.toProfileResponse(testUser, 10L, 5L)).thenReturn(expectedResponse);

        // When
        UserProfileResponse result = userService.getProfile(userId);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getProfile_NonExistingUser_ThrowsException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getProfile(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining(userId.toString());
    }

    @Test
    void updateProfile_UpdatesFields() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest("newusername", "New bio", "http://example.com/newavatar.jpg");
        when(securityUtils.getCurrentKeycloakId()).thenReturn("test-keycloak-id");
        when(userRepository.findByKeycloakId("test-keycloak-id")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(followRepository.countByFollowing(testUser)).thenReturn(10L);
        when(followRepository.countByFollower(testUser)).thenReturn(5L);
        UserProfileResponse expectedResponse = new UserProfileResponse(
                userId, "newusername", "test@example.com", "New bio",
                "http://example.com/newavatar.jpg", 10L, 5L, testUser.getCreatedAt()
        );
        when(userMapper.toProfileResponse(testUser, 10L, 5L)).thenReturn(expectedResponse);

        // When
        UserProfileResponse result = userService.updateProfile(request);

        // Then
        assertThat(testUser.getUsername()).isEqualTo("newusername");
        assertThat(testUser.getBio()).isEqualTo("New bio");
        assertThat(testUser.getAvatarUrl()).isEqualTo("http://example.com/newavatar.jpg");
        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository).save(testUser);
    }

    @Test
    void findById_ExistingUser_ReturnsUser() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.findById(userId);

        // Then
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void findById_NonExistingUser_ThrowsException() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining(userId.toString());
    }
}
