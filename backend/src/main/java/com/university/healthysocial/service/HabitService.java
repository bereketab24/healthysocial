package com.university.healthysocial.service;

import com.university.healthysocial.domain.Habit;
import com.university.healthysocial.domain.HabitLog;
import com.university.healthysocial.domain.User;
import com.university.healthysocial.dto.request.Requests.CreateHabitLogRequest;
import com.university.healthysocial.dto.request.Requests.CreateHabitRequest;
import com.university.healthysocial.dto.request.Requests.UpdateHabitRequest;
import com.university.healthysocial.dto.response.Responses.HabitLogResponse;
import com.university.healthysocial.dto.response.Responses.HabitResponse;
import com.university.healthysocial.dto.response.Responses.PageResponse;
import com.university.healthysocial.exception.ForbiddenOperationException;
import com.university.healthysocial.exception.ResourceNotFoundException;
import com.university.healthysocial.mapper.HabitMapper;
import com.university.healthysocial.repository.HabitLogRepository;
import com.university.healthysocial.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final UserService userService;
    private final HabitMapper habitMapper;

    public PageResponse<HabitResponse> getMyHabits(boolean activeOnly, Pageable pageable) {
        User currentUser = userService.resolveCurrentUser();
        Page<Habit> habits;
        if (activeOnly) {
            habits = habitRepository.findAllByUserAndIsActive(currentUser, true, pageable);
        } else {
            habits = habitRepository.findAllByUser(currentUser, pageable);
        }
        
        return new PageResponse<>(
                habits.getContent().stream().map(this::mapToResponse).toList(),
                habits.getNumber(),
                habits.getSize(),
                habits.getTotalElements(),
                habits.getTotalPages(),
                habits.isLast()
        );
    }

    @Transactional
    public HabitResponse createHabit(CreateHabitRequest req) {
        User currentUser = userService.resolveCurrentUser();
        Habit habit = Habit.builder()
                .user(currentUser)
                .name(req.name())
                .description(req.description())
                .frequency(req.frequency())
                .category(req.category())
                .targetCount(req.targetCount())
                .build();
        return mapToResponse(habitRepository.save(habit));
    }

    @Transactional
    public HabitResponse updateHabit(UUID habitId, UpdateHabitRequest req) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit", habitId));
        
        User currentUser = userService.resolveCurrentUser();
        if (!habit.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only update your own habits");
        }
        
        if (req.name() != null) habit.setName(req.name());
        if (req.description() != null) habit.setDescription(req.description());
        if (req.targetCount() != null) habit.setTargetCount(req.targetCount());
        if (req.isActive() != null) habit.setActive(req.isActive());
        
        return mapToResponse(habitRepository.save(habit));
    }

    public HabitResponse getHabit(UUID habitId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit", habitId));
        return mapToResponse(habit);
    }

    @Transactional
    public void deleteHabit(UUID habitId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit", habitId));
        
        User currentUser = userService.resolveCurrentUser();
        if (!habit.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only delete your own habits");
        }
        
        habitRepository.delete(habit);
    }

    public PageResponse<HabitLogResponse> getHabitLogs(UUID habitId, LocalDate from, LocalDate to, Pageable pageable) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit", habitId));
        
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        
        Page<HabitLog> logs = habitLogRepository.findAllByHabitAndLoggedDateBetween(habit, from, to, pageable);
        
        return new PageResponse<>(
                logs.getContent().stream().map(habitMapper::toLogResponse).toList(),
                logs.getNumber(),
                logs.getSize(),
                logs.getTotalElements(),
                logs.getTotalPages(),
                logs.isLast()
        );
    }

    @Transactional
    public HabitLogResponse logHabit(CreateHabitLogRequest req) {
        Habit habit = habitRepository.findById(req.habitId())
                .orElseThrow(() -> new ResourceNotFoundException("Habit", req.habitId()));
        
        User currentUser = userService.resolveCurrentUser();
        if (!habit.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only log your own habits");
        }
        
        // Check if already logged today
        OffsetDateTime startOfDay = LocalDate.now().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        if (habitLogRepository.existsByHabitAndCreatedAtAfter(habit, startOfDay)) {
            // Depending on requirements, we might allow multiple logs or not. 
            // Let's assume one log per day for simplicity of streak.
        }

        HabitLog log = HabitLog.builder()
                .habit(habit)
                .user(currentUser)
                .note(req.note())
                .completed(true)
                .loggedDate(LocalDate.now())
                .build();
        
        return habitMapper.toLogResponse(habitLogRepository.save(log));
    }

    private HabitResponse mapToResponse(Habit habit) {
        List<HabitLog> logs = habitLogRepository.findAllByHabit(habit);
        int currentStreak = calculateStreak(logs);
        // Longest streak would need more complex logic or a dedicated field
        return habitMapper.toResponse(habit, currentStreak, currentStreak);
    }

    private int calculateStreak(List<HabitLog> logs) {
        if (logs.isEmpty()) return 0;
        
        List<LocalDate> dates = logs.stream()
                .map(HabitLog::getLoggedDate)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
        
        int streak = 0;
        LocalDate today = LocalDate.now();
        LocalDate expected = dates.get(0);
        
        // If the last log was not today or yesterday, streak is broken
        if (!expected.equals(today) && !expected.equals(today.minusDays(1))) {
            return 0;
        }
        
        for (LocalDate date : dates) {
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        
        return streak;
    }
}
