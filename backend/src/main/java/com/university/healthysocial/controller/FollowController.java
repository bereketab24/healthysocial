package com.university.healthysocial.controller;

import com.university.healthysocial.dto.response.Responses.PageResponse;
import com.university.healthysocial.dto.response.Responses.UserProfileResponse;
import com.university.healthysocial.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Follows", description = "Follow and unfollow other users")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Follow a user")
    public ResponseEntity<Void> follow(@PathVariable UUID userId) {
        followService.follow(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unfollow a user")
    public ResponseEntity<Void> unfollow(@PathVariable UUID userId) {
        followService.unfollow(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/followers")
    @Operation(summary = "List followers of a user")
    public ResponseEntity<PageResponse<UserProfileResponse>> getFollowers(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                followService.getFollowers(userId, PageRequest.of(page, size)));
    }

    @GetMapping("/{userId}/following")
    @Operation(summary = "List users that a user is following")
    public ResponseEntity<PageResponse<UserProfileResponse>> getFollowing(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                followService.getFollowing(userId, PageRequest.of(page, size)));
    }

    @GetMapping("/{userId}/is-following")
    @Operation(summary = "Check if the current user follows a given user")
    public ResponseEntity<Map<String, Boolean>> isFollowing(@PathVariable UUID userId) {
        return ResponseEntity.ok(Map.of("following", followService.isFollowing(userId)));
    }
}