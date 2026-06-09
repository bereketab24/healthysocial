package com.university.healthysocial.analytics;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class AnalyticsDtos {

    private AnalyticsDtos() {}

    public record TopUserRow(
            String username,
            long postCount,
            long commentCount,
            long likesReceived,
            long activityScore
    ) {}

    public record EngagementDayRow(
            LocalDate day,
            long posts,
            long comments,
            long likes
    ) {}

    public record ChallengeStatRow(
            String title,
            String category,
            long participants,
            long completed,
            double completionRate
    ) {}

    public record HabitStreakRow(
            String username,
            String habitName,
            long completedLogs
    ) {}

    public record PostEngagementRow(
            String postId,
            String authorUsername,
            String preview,
            long likes,
            long comments,
            long engagement
    ) {}
}
