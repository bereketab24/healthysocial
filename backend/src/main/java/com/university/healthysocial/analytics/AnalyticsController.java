package com.university.healthysocial.analytics;

import com.university.healthysocial.analytics.AnalyticsDtos.ChallengeStatRow;
import com.university.healthysocial.analytics.AnalyticsDtos.EngagementDayRow;
import com.university.healthysocial.analytics.AnalyticsDtos.HabitStreakRow;
import com.university.healthysocial.analytics.AnalyticsDtos.PostEngagementRow;
import com.university.healthysocial.analytics.AnalyticsDtos.TopUserRow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Aggregated analytics powered by BigQuery (public, read-only)")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/top-users")
    @Operation(summary = "Top users by combined post/comment/like activity")
    public ResponseEntity<List<TopUserRow>> topUsers(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopUsers(clamp(limit, 50)));
    }

    @GetMapping("/engagement")
    @Operation(summary = "Daily posts/comments/likes for the last N days")
    public ResponseEntity<List<EngagementDayRow>> engagement(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getEngagementTimeline(clamp(days, 90)));
    }

    @GetMapping("/challenge-stats")
    @Operation(summary = "Participation and completion rate per challenge")
    public ResponseEntity<List<ChallengeStatRow>> challengeStats(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getChallengeStats(clamp(limit, 50)));
    }

    @GetMapping("/habit-streaks")
    @Operation(summary = "Top habit/user pairs by completed log count")
    public ResponseEntity<List<HabitStreakRow>> habitStreaks(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getHabitStreaks(clamp(limit, 50)));
    }

    @GetMapping("/post-engagement")
    @Operation(summary = "Top posts by likes + comments")
    public ResponseEntity<List<PostEngagementRow>> postEngagement(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getPostEngagement(clamp(limit, 50)));
    }

    private static int clamp(int v, int max) {
        if (v < 1) return 1;
        return Math.min(v, max);
    }
}
