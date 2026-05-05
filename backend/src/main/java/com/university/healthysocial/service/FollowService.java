package com.university.healthysocial.service;

import com.university.healthysocial.domain.Follow;
import com.university.healthysocial.domain.User;
import com.university.healthysocial.dto.response.Responses.PageResponse;
import com.university.healthysocial.dto.response.Responses.UserProfileResponse;
import com.university.healthysocial.exception.ForbiddenOperationException;
import com.university.healthysocial.mapper.UserMapper;
import com.university.healthysocial.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserService userService;
    private final UserMapper userMapper;

    public PageResponse<UserProfileResponse> getFollowers(UUID userId, Pageable pageable) {
        User user = userService.findById(userId);
        Page<Follow> follows = followRepository.findAllByFollowing(user, pageable);
        
        return new PageResponse<>(
                follows.getContent().stream()
                        .map(f -> {
                            User follower = f.getFollower();
                            return userMapper.toProfileResponse(follower, 
                                    followRepository.countByFollowing(follower),
                                    followRepository.countByFollower(follower));
                        }).toList(),
                follows.getNumber(),
                follows.getSize(),
                follows.getTotalElements(),
                follows.getTotalPages(),
                follows.isLast()
        );
    }

    public PageResponse<UserProfileResponse> getFollowing(UUID userId, Pageable pageable) {
        User user = userService.findById(userId);
        Page<Follow> follows = followRepository.findAllByFollower(user, pageable);
        
        return new PageResponse<>(
                follows.getContent().stream()
                        .map(f -> {
                            User following = f.getFollowing();
                            return userMapper.toProfileResponse(following,
                                    followRepository.countByFollowing(following),
                                    followRepository.countByFollower(following));
                        }).toList(),
                follows.getNumber(),
                follows.getSize(),
                follows.getTotalElements(),
                follows.getTotalPages(),
                follows.isLast()
        );
    }

    @Transactional
    public void follow(UUID userId) {
        User currentUser = userService.resolveCurrentUser();
        User toFollow = userService.findById(userId);
        
        if (currentUser.getId().equals(toFollow.getId())) {
            throw new ForbiddenOperationException("You cannot follow yourself");
        }
        
        if (followRepository.existsByFollowerAndFollowing(currentUser, toFollow)) {
            return; // Already following
        }
        
        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(toFollow)
                .build();
        
        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(UUID userId) {
        User currentUser = userService.resolveCurrentUser();
        User toUnfollow = userService.findById(userId);
        
        followRepository.findByFollowerAndFollowing(currentUser, toUnfollow)
                .ifPresent(followRepository::delete);
    }

    public boolean isFollowing(UUID userId) {
        try {
            User currentUser = userService.resolveCurrentUser();
            User target = userService.findById(userId);
            return followRepository.existsByFollowerAndFollowing(currentUser, target);
        } catch (Exception e) {
            return false;
        }
    }
}
