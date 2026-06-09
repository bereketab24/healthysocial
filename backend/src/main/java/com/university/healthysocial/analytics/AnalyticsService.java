package com.university.healthysocial.analytics;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.university.healthysocial.analytics.AnalyticsDtos.ChallengeStatRow;
import com.university.healthysocial.analytics.AnalyticsDtos.EngagementDayRow;
import com.university.healthysocial.analytics.AnalyticsDtos.HabitStreakRow;
import com.university.healthysocial.analytics.AnalyticsDtos.PostEngagementRow;
import com.university.healthysocial.analytics.AnalyticsDtos.TopUserRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Reads aggregated analytics from BigQuery.
 *
 * <p>When {@code bigquery.enabled=false} or the project is misconfigured,
 * every method returns an empty list rather than failing — this keeps the API
 * responsive while the data pipeline is being provisioned.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ObjectProvider<BigQuery> bigQueryProvider;
    private final BigQueryProperties props;

    private String dataset() {
        return "`" + props.projectId() + "." + props.dataset() + "`";
    }

    public List<TopUserRow> getTopUsers(int limit) {
        String sql = """
                WITH
                  post_counts AS (
                    SELECT user_id, COUNT(*) AS posts FROM %1$s.posts GROUP BY user_id
                  ),
                  comment_counts AS (
                    SELECT user_id, COUNT(*) AS comments FROM %1$s.comments GROUP BY user_id
                  ),
                  likes_received AS (
                    SELECT p.user_id, COUNT(*) AS likes_received
                    FROM %1$s.likes l
                    JOIN %1$s.posts p ON l.post_id = p.id
                    GROUP BY p.user_id
                  )
                SELECT
                  u.username,
                  COALESCE(pc.posts, 0) AS post_count,
                  COALESCE(cc.comments, 0) AS comment_count,
                  COALESCE(lr.likes_received, 0) AS likes_received,
                  COALESCE(pc.posts, 0) + COALESCE(cc.comments, 0) + COALESCE(lr.likes_received, 0) AS activity_score
                FROM %1$s.users u
                LEFT JOIN post_counts pc ON pc.user_id = u.id
                LEFT JOIN comment_counts cc ON cc.user_id = u.id
                LEFT JOIN likes_received lr ON lr.user_id = u.id
                ORDER BY activity_score DESC
                LIMIT @lim
                """.formatted(dataset());
        return query(sql, limit, row -> new TopUserRow(
                row.get("username").getStringValue(),
                row.get("post_count").getLongValue(),
                row.get("comment_count").getLongValue(),
                row.get("likes_received").getLongValue(),
                row.get("activity_score").getLongValue()
        ));
    }

    public List<EngagementDayRow> getEngagementTimeline(int days) {
        String sql = """
                WITH days AS (
                  SELECT day FROM UNNEST(
                    GENERATE_DATE_ARRAY(DATE_SUB(CURRENT_DATE(), INTERVAL @lim - 1 DAY), CURRENT_DATE())
                  ) AS day
                ),
                  posts_by_day AS (
                    SELECT DATE(created_at) AS day, COUNT(*) AS n FROM %1$s.posts GROUP BY day
                  ),
                  comments_by_day AS (
                    SELECT DATE(created_at) AS day, COUNT(*) AS n FROM %1$s.comments GROUP BY day
                  ),
                  likes_by_day AS (
                    SELECT DATE(created_at) AS day, COUNT(*) AS n FROM %1$s.likes GROUP BY day
                  )
                SELECT
                  d.day,
                  COALESCE(p.n, 0) AS posts,
                  COALESCE(c.n, 0) AS comments,
                  COALESCE(l.n, 0) AS likes
                FROM days d
                LEFT JOIN posts_by_day p ON p.day = d.day
                LEFT JOIN comments_by_day c ON c.day = d.day
                LEFT JOIN likes_by_day l ON l.day = d.day
                ORDER BY d.day
                """.formatted(dataset());
        return query(sql, days, row -> new EngagementDayRow(
                LocalDate.parse(row.get("day").getStringValue()),
                row.get("posts").getLongValue(),
                row.get("comments").getLongValue(),
                row.get("likes").getLongValue()
        ));
    }

    public List<ChallengeStatRow> getChallengeStats(int limit) {
        String sql = """
                SELECT
                  c.title,
                  c.category,
                  COUNT(cp.id) AS participants,
                  COUNTIF(cp.is_completed) AS completed,
                  SAFE_DIVIDE(COUNTIF(cp.is_completed), COUNT(cp.id)) AS completion_rate
                FROM %1$s.challenges c
                LEFT JOIN %1$s.challenge_participants cp ON cp.challenge_id = c.id
                GROUP BY c.id, c.title, c.category
                ORDER BY participants DESC, completion_rate DESC
                LIMIT @lim
                """.formatted(dataset());
        return query(sql, limit, row -> new ChallengeStatRow(
                row.get("title").getStringValue(),
                row.get("category").getStringValue(),
                row.get("participants").getLongValue(),
                row.get("completed").getLongValue(),
                row.get("completion_rate").isNull() ? 0.0 : row.get("completion_rate").getDoubleValue()
        ));
    }

    public List<HabitStreakRow> getHabitStreaks(int limit) {
        String sql = """
                SELECT
                  u.username,
                  h.name AS habit_name,
                  COUNT(hl.id) AS completed_logs
                FROM %1$s.habit_logs hl
                JOIN %1$s.habits h ON hl.habit_id = h.id
                JOIN %1$s.users u ON hl.user_id = u.id
                WHERE hl.completed = TRUE
                GROUP BY u.username, h.name
                ORDER BY completed_logs DESC
                LIMIT @lim
                """.formatted(dataset());
        return query(sql, limit, row -> new HabitStreakRow(
                row.get("username").getStringValue(),
                row.get("habit_name").getStringValue(),
                row.get("completed_logs").getLongValue()
        ));
    }

    public List<PostEngagementRow> getPostEngagement(int limit) {
        String sql = """
                SELECT
                  p.id AS post_id,
                  u.username AS author,
                  SUBSTR(p.content, 1, 80) AS preview,
                  COALESCE(l.likes, 0) AS likes,
                  COALESCE(c.comments, 0) AS comments,
                  COALESCE(l.likes, 0) + COALESCE(c.comments, 0) AS engagement
                FROM %1$s.posts p
                JOIN %1$s.users u ON u.id = p.user_id
                LEFT JOIN (SELECT post_id, COUNT(*) AS likes FROM %1$s.likes GROUP BY post_id) l ON l.post_id = p.id
                LEFT JOIN (SELECT post_id, COUNT(*) AS comments FROM %1$s.comments GROUP BY post_id) c ON c.post_id = p.id
                ORDER BY engagement DESC, p.created_at DESC
                LIMIT @lim
                """.formatted(dataset());
        return query(sql, limit, row -> new PostEngagementRow(
                row.get("post_id").getStringValue(),
                row.get("author").getStringValue(),
                row.get("preview").isNull() ? "" : row.get("preview").getStringValue(),
                row.get("likes").getLongValue(),
                row.get("comments").getLongValue(),
                row.get("engagement").getLongValue()
        ));
    }

    private <T> List<T> query(String sql, int limit, Function<FieldValueList, T> mapper) {
        if (!props.enabled()) {
            log.debug("BigQuery disabled (bigquery.enabled=false), returning empty result");
            return Collections.emptyList();
        }
        if (props.projectId() == null || props.projectId().isBlank()) {
            log.warn("bigquery.project-id is not set; returning empty result");
            return Collections.emptyList();
        }
        BigQuery bigQuery = bigQueryProvider.getIfAvailable();
        if (bigQuery == null) {
            log.warn("BigQuery client bean unavailable despite bigquery.enabled=true; returning empty result");
            return Collections.emptyList();
        }
        try {
            QueryJobConfiguration cfg = QueryJobConfiguration.newBuilder(sql)
                    .addNamedParameter("lim", com.google.cloud.bigquery.QueryParameterValue.int64(limit))
                    .setUseLegacySql(false)
                    .build();
            TableResult result = bigQuery.query(cfg);
            List<T> out = new ArrayList<>();
            for (FieldValueList row : result.iterateAll()) {
                out.add(mapper.apply(row));
            }
            return out;
        } catch (BigQueryException | InterruptedException e) {
            log.error("BigQuery query failed: {}", e.getMessage(), e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return Collections.emptyList();
        }
    }
}
