package com.university.healthysocial.controller;

import com.university.healthysocial.dto.request.Requests.CreateGoalRequest;
import com.university.healthysocial.dto.request.Requests.UpdateGoalProgressRequest;
import com.university.healthysocial.dto.response.Responses.GoalResponse;
import com.university.healthysocial.dto.response.Responses.PageResponse;
import com.university.healthysocial.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Set and track personal health goals")
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    @Operation(summary = "Create a new goal")
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody CreateGoalRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.createGoal(req));
    }

    @GetMapping
    @Operation(summary = "List all my goals (paginated)")
    public ResponseEntity<PageResponse<GoalResponse>> getMyGoals(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(goalService.getMyGoals(pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "List my active (incomplete) goals")
    public ResponseEntity<List<GoalResponse>> getActiveGoals() {
        return ResponseEntity.ok(goalService.getMyActiveGoals());
    }

    @GetMapping("/{goalId}")
    @Operation(summary = "Get a single goal")
    public ResponseEntity<GoalResponse> getGoal(@PathVariable UUID goalId) {
        return ResponseEntity.ok(goalService.getGoal(goalId));
    }

    @PatchMapping("/{goalId}/progress")
    @Operation(summary = "Update goal progress (0–100). Setting to 100 marks it complete.")
    public ResponseEntity<GoalResponse> updateProgress(
            @PathVariable UUID goalId,
            @Valid @RequestBody UpdateGoalProgressRequest req) {
        return ResponseEntity.ok(goalService.updateProgress(goalId, req));
    }

    @DeleteMapping("/{goalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a goal")
    public ResponseEntity<Void> deleteGoal(@PathVariable UUID goalId) {
        goalService.deleteGoal(goalId);
        return ResponseEntity.noContent().build();
    }
}