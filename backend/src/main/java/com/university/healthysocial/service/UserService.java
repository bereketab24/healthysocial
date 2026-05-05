package com.university.healthysocial.service;

import com.university.healthysocial.domain.User;
import com.university.healthysocial.dto.request.Requests.UpdateProfileRequest;
import com.university.healthysocial.dto.response.Responses.UserProfileResponse;
import com.university.healthysocial.exception.ResourceNotFoundException;
import com.university.healthysocial.mapper.UserMapper;
import com.university.healthysocial.repository.FollowRepository;
import com.university.healthysocial.repository.UserRepository;
import com.university.healthysocial.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    @Transactional
    public User resolveCurrentUser() {
        String keycloakId = securityUtils.getCurrentKeycloakId();
        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .keycloakId(keycloakId)
                            .username(securityUtils.getCurrentPreferredUsername().orElse("user_" + UUID.randomUUID().toString().substring(0, 8)))
                            .email(securityUtils.getCurrentEmail().orElseThrow(() -> new IllegalStateException("Email missing in JWT")))
                            .build();
                    return userRepository.save(newUser);
                });
    }

    public UserProfileResponse getMyProfile() {
        User user = resolveCurrentUser();
        return getProfile(user.getId());
    }

    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        long followersCount = followRepository.countByFollowing(user);
        long followingCount = followRepository.countByFollower(user);
        
        return userMapper.toProfileResponse(user, followersCount, followingCount);
    }

    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest req) {
        User user = resolveCurrentUser();
        
        if (req.username() != null) user.setUsername(req.username());
        if (req.bio() != null) user.setBio(req.bio());
        if (req.avatarUrl() != null) user.setAvatarUrl(req.avatarUrl());
        
        User saved = userRepository.save(user);
        return getProfile(saved.getId());
    }
    
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
