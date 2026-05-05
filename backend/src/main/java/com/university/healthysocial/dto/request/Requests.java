package com.university.healthysocial.dto.request;

import com.university.healthysocial.domain.enums.HabitCategory;
import com.university.healthysocial.domain.enums.HabitFrequency;
import com.university.healthysocial.domain.enums.GoalCategory;
import com.university.healthysocial.domain.enums.PostType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

// ════════════════════════════════════════════════════════════════════════════
// User
// ════════════════════════════════════════════════════════════════════════════

public sealed interface Requests permits
        Requests.UpdateProfileRequest,
        Requests.CreateHabitRequest,
        Requests.UpdateHabitRequest,
        Requests.CreateHabitLogRequest,
        Requests.CreateGoalRequest,
        Requests.UpdateGoalProgressRequest,
        Requests.CreatePostRequest,
        Requests.UpdatePostRequest,
        Requests.CreateCommentRequest,
        Requests.CreateChallengeRequest {

    record UpdateProfileRequest(
            @Size(min = 3, max = 60, message = "Username must be 3–60 characters")
            String username,

            @Size(max = 500, message = "Bio must not exceed 500 characters")
            String bio,

            @Size(max = 2048)
            String avatarUrl
    ) implements Requests {}

    // ── Habit ────────────────────────────────────────────────────────────────

    record CreateHabitRequest(
            @NotBlank(message = "Habit name is required")
            @Size(max = 120)
            String name,

            @Size(max = 500)
            String description,

            @NotNull(message = "Frequency is required")
            HabitFrequency frequency,

            @NotNull(message = "Category is required")
            HabitCategory category,

            @Min(value = 1, message = "Target count must be at least 1")
            @Max(value = 100, message = "Target count must not exceed 100")
            int targetCount
    ) implements Requests {}

    record UpdateHabitRequest(
            @Size(min = 1, max = 120)
            String name,

            @Size(max = 500)
            String description,

            @Min(1) @Max(100)
            Integer targetCount,

            Boolean isActive
    ) implements Requests {}

    // ── Habit Log ────────────────────────────────────────────────────────────

    record CreateHabitLogRequest(
            @NotNull(message = "Habit ID is required")
            UUID habitId,

            @Size(max = 500)
            String note
    ) implements Requests {}

    // ── Goal ─────────────────────────────────────────────────────────────────

    record CreateGoalRequest(
            @NotBlank(message = "Title is required")
            @Size(max = 150)
            String title,

            @Size(max = 1000)
            String description,

            @Future(message = "Target date must be in the future")
            LocalDate targetDate,

            @NotNull(message = "Category is required")
            GoalCategory category
    ) implements Requests {}

    record UpdateGoalProgressRequest(
            @NotNull
            @Min(value = 0, message = "Progress must be 0–100")
            @Max(value = 100, message = "Progress must be 0–100")
            Integer progressPercentage
    ) implements Requests {}

    // ── Post ─────────────────────────────────────────────────────────────────

    record CreatePostRequest(
            @NotBlank(message = "Content is required")
            @Size(max = 5000, message = "Post content must not exceed 5000 characters")
            String content,

            @Size(max = 2048)
            String imageUrl,

            @NotNull(message = "Post type is required")
            PostType postType
    ) implements Requests {}

    record UpdatePostRequest(
            @NotBlank
            @Size(max = 5000)
            String content,

            @Size(max = 2048)
            String imageUrl
    ) implements Requests {}

    // ── Comment ──────────────────────────────────────────────────────────────

    record CreateCommentRequest(
            @NotBlank(message = "Comment content is required")
            @Size(max = 1000)
            String content
    ) implements Requests {}

    // ── Challenge ────────────────────────────────────────────────────────────

    record CreateChallengeRequest(
            @NotBlank(message = "Title is required")
            @Size(max = 150)
            String title,

            @NotBlank(message = "Description is required")
            @Size(max = 1000)
            String description,

            @NotNull(message = "Start date is required")
            LocalDate startDate,

            @NotNull(message = "End date is required")
            LocalDate endDate,

            @NotNull(message = "Category is required")
            HabitCategory category
    ) implements Requests {}
}