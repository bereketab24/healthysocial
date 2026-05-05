package com.university.healthysocial.service;

import com.university.healthysocial.domain.Goal;
import com.university.healthysocial.domain.User;
import com.university.healthysocial.dto.request.Requests.CreateGoalRequest;
import com.university.healthysocial.dto.request.Requests.UpdateGoalProgressRequest;
import com.university.healthysocial.dto.response.Responses.GoalResponse;
import com.university.healthysocial.dto.response.Responses.PageResponse;
import com.university.healthysocial.exception.ForbiddenOperationException;
import com.university.healthysocial.exception.ResourceNotFoundException;
import com.university.healthysocial.mapper.GoalMapper;
import com.university.healthysocial.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserService userService;
    private final GoalMapper goalMapper;

    public PageResponse<GoalResponse> getMyGoals(Pageable pageable) {
        User currentUser = userService.resolveCurrentUser();
        Page<Goal> goals = goalRepository.findAllByUser(currentUser, pageable);
        
        return new PageResponse<>(
                goals.getContent().stream().map(goalMapper::toResponse).toList(),
                goals.getNumber(),
                goals.getSize(),
                goals.getTotalElements(),
                goals.getTotalPages(),
                goals.isLast()
        );
    }

    public List<GoalResponse> getMyActiveGoals() {
        User currentUser = userService.resolveCurrentUser();
        return goalRepository.findAllByUserAndIsCompleted(currentUser, false)
                .stream()
                .map(goalMapper::toResponse)
                .toList();
    }

    public GoalResponse getGoal(UUID goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));
        return goalMapper.toResponse(goal);
    }

    @Transactional
    public GoalResponse createGoal(CreateGoalRequest req) {
        User currentUser = userService.resolveCurrentUser();
        Goal goal = Goal.builder()
                .user(currentUser)
                .title(req.title())
                .description(req.description())
                .targetDate(req.targetDate())
                .category(req.category())
                .progressPercentage(0)
                .build();
        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Transactional
    public GoalResponse updateProgress(UUID goalId, UpdateGoalProgressRequest req) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));
        
        User currentUser = userService.resolveCurrentUser();
        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only update your own goals");
        }
        
        goal.setProgressPercentage(req.progressPercentage());
        if (req.progressPercentage() == 100) {
            goal.setCompleted(true);
            goal.setCompletedAt(OffsetDateTime.now());
        } else {
            goal.setCompleted(false);
            goal.setCompletedAt(null);
        }
        
        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Transactional
    public void deleteGoal(UUID goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));
        
        User currentUser = userService.resolveCurrentUser();
        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only delete your own goals");
        }
        
        goalRepository.delete(goal);
    }
}
