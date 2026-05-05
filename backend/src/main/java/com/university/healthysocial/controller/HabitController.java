package com.university.healthysocial.controller;

import com.university.healthysocial.dto.request.Requests.CreateHabitLogRequest;
import com.university.healthysocial.dto.request.Requests.CreateHabitRequest;
import com.university.healthysocial.dto.request.Requests.UpdateHabitRequest;
import com.university.healthysocial.dto.response.Responses.HabitLogResponse;
import com.university.healthysocial.dto.response.Responses.HabitResponse;
import com.university.healthysocial.dto.response.Responses.PageResponse;
import com.university.healthysocial.service.HabitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
@Tag(name = "Habits", description = "Habit tracking – create, update, log and monitor streaks")
public class HabitController {

    private final HabitService habitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new habit")
    public ResponseEntity<HabitResponse> createHabit(@Valid @RequestBody CreateHabitRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(habitService.createHabit(req));
    }

    @GetMapping
    @Operation(summary = "List my habits (optionally filter to active only)")
    public ResponseEntity<PageResponse<HabitResponse>> getMyHabits(
            @RequestParam(defaultValue = "true")  boolean activeOnly,
            @RequestParam(defaultValue = "0")     int page,
            @RequestParam(defaultValue = "20")    int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(habitService.getMyHabits(activeOnly, pageable));
    }

    @GetMapping("/{habitId}")
    @Operation(summary = "Get a single habit by ID")
    public ResponseEntity<HabitResponse> getHabit(@PathVariable UUID habitId) {
        return ResponseEntity.ok(habitService.getHabit(habitId));
    }

    @PatchMapping("/{habitId}")
    @Operation(summary = "Update a habit (partial update)")
    public ResponseEntity<HabitResponse> updateHabit(
            @PathVariable UUID habitId,
            @Valid @RequestBody UpdateHabitRequest req) {
        return ResponseEntity.ok(habitService.updateHabit(habitId, req));
    }

    @DeleteMapping("/{habitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a habit and all its logs")
    public ResponseEntity<Void> deleteHabit(@PathVariable UUID habitId) {
        habitService.deleteHabit(habitId);
        return ResponseEntity.noContent().build();
    }

    // ── Logging ───────────────────────────────────────────────────────────────

    @PostMapping("/logs")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Log a habit as completed for today")
    public ResponseEntity<HabitLogResponse> logHabit(@Valid @RequestBody CreateHabitLogRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(habitService.logHabit(req));
    }

    @GetMapping("/{habitId}/logs")
    @Operation(summary = "Get habit logs for a date range (defaults to last 30 days)")
    public ResponseEntity<PageResponse<HabitLogResponse>> getHabitLogs(
            @PathVariable UUID habitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(habitService.getHabitLogs(habitId, from, to, pageable));
    }
}