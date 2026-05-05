package com.university.healthysocial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.university.healthysocial.domain.enums.GoalCategory;
import com.university.healthysocial.domain.enums.HabitCategory;
import com.university.healthysocial.domain.enums.HabitFrequency;
import com.university.healthysocial.domain.enums.PostType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * All API response DTOs in one file for convenience.
 * Every type is a Java {@code record} — immutable, serialisation-friendly,
 * and free of boilerplate.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Responses {

    private Responses() {}

    // ── User ─────────────────────────────────────────────────────────────────

    public record UserProfileResponse(
            UUID id,
            String username,
            String email,
            String bio,
            String avatarUrl,
            long followersCount,
            long followingCount,
            OffsetDateTime createdAt
    ) {}

    // ── Habit ────────────────────────────────────────────────────────────────

    public record HabitResponse(
            UUID id,
            String name,
            String description,
            HabitFrequency frequency,
            HabitCategory category,
            int targetCount,
            boolean isActive,
            int currentStreak,
            int longestStreak,
            OffsetDateTime createdAt
    ) {}

    // ── Habit Log ────────────────────────────────────────────────────────────

    public record HabitLogResponse(
            UUID id,
            UUID habitId,
            String habitName,
            LocalDate loggedDate,
            String note,
            boolean completed,
            OffsetDateTime createdAt
    ) {}

    // ── Goal ─────────────────────────────────────────────────────────────────

    public record GoalResponse(
            UUID id,
            String title,
            String description,
            LocalDate targetDate,
            GoalCategory category,
            int progressPercentage,
            boolean isCompleted,
            OffsetDateTime completedAt,
            OffsetDateTime createdAt
    ) {}

    // ── Post ─────────────────────────────────────────────────────────────────

    public record PostResponse(
            UUID id,
            UUID authorId,
            String authorUsername,
            String authorAvatarUrl,
            String content,
            String imageUrl,
            PostType postType,
            long likesCount,
            long commentsCount,
            boolean likedByCurrentUser,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {}

    // ── Comment ──────────────────────────────────────────────────────────────

    public record CommentResponse(
            UUID id,
            UUID postId,
            UUID authorId,
            String authorUsername,
            String authorAvatarUrl,
            String content,
            OffsetDateTime createdAt
    ) {}

    // ── Follow ───────────────────────────────────────────────────────────────

    public record FollowResponse(
            UUID id,
            UUID followerId,
            String followerUsername,
            UUID followingId,
            String followingUsername,
            OffsetDateTime createdAt
    ) {}

    // ── Challenge ────────────────────────────────────────────────────────────

    public record ChallengeResponse(
            UUID id,
            UUID creatorId,
            String creatorUsername,
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            HabitCategory category,
            long participantsCount,
            boolean isJoined,
            boolean isActive
    ) {}

    // ── Leaderboard ──────────────────────────────────────────────────────────

    public record LeaderboardEntryResponse(
            int rank,
            UUID userId,
            String username,
            String avatarUrl,
            long totalHabitsCompleted,
            int currentStreak
    ) {}

    // ── Generic Page Wrapper ─────────────────────────────────────────────────

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {}
}