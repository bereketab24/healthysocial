package com.university.healthysocial.controller;

import com.university.healthysocial.domain.enums.HabitCategory;
import com.university.healthysocial.dto.request.Requests.CreateChallengeRequest;
import com.university.healthysocial.dto.response.Responses.ChallengeResponse;
import com.university.healthysocial.dto.response.Responses.PageResponse;
import com.university.healthysocial.service.ChallengeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Tag(name = "Challenges", description = "Community wellness challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping
    @Operation(summary = "Browse all challenges (filterable by category and active status — no auth required)")
    public ResponseEntity<PageResponse<ChallengeResponse>> getChallenges(
            @RequestParam(required = false)    HabitCategory category,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                challengeService.getAllChallenges(category, activeOnly, pageable));
    }

    @GetMapping("/{challengeId}")
    @Operation(summary = "Get a challenge by ID (no auth required)")
    public ResponseEntity<ChallengeResponse> getChallenge(@PathVariable UUID challengeId) {
        return ResponseEntity.ok(challengeService.getChallenge(challengeId));
    }

    @PostMapping
    @Operation(summary = "Create a new community challenge")
    public ResponseEntity<ChallengeResponse> createChallenge(
            @Valid @RequestBody CreateChallengeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(challengeService.createChallenge(req));
    }

    @DeleteMapping("/{challengeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a challenge (creator only)")
    public ResponseEntity<Void> deleteChallenge(@PathVariable UUID challengeId) {
        challengeService.deleteChallenge(challengeId);
        return ResponseEntity.noContent().build();
    }

    // ── Participation ─────────────────────────────────────────────────────────

    @PostMapping("/{challengeId}/join")
    @Operation(summary = "Join a challenge")
    public ResponseEntity<ChallengeResponse> join(@PathVariable UUID challengeId) {
        return ResponseEntity.ok(challengeService.joinChallenge(challengeId));
    }

    @DeleteMapping("/{challengeId}/join")
    @Operation(summary = "Leave a challenge")
    public ResponseEntity<ChallengeResponse> leave(@PathVariable UUID challengeId) {
        return ResponseEntity.ok(challengeService.leaveChallenge(challengeId));
    }

    @PostMapping("/{challengeId}/complete")
    @Operation(summary = "Mark the challenge as completed for yourself")
    public ResponseEntity<ChallengeResponse> markComplete(@PathVariable UUID challengeId) {
        return ResponseEntity.ok(challengeService.markCompleted(challengeId));
    }
}