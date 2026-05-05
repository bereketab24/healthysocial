package com.university.healthysocial.service;

import com.university.healthysocial.domain.User;
import com.university.healthysocial.dto.response.Responses.LeaderboardEntryResponse;
import com.university.healthysocial.repository.HabitLogRepository;
import com.university.healthysocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {

    private final UserRepository userRepository;
    private final HabitLogRepository habitLogRepository;

    public List<LeaderboardEntryResponse> getLeaderboard(int limit) {
        List<User> topUsers = userRepository.findLeaderboard();
        AtomicInteger rank = new AtomicInteger(1);
        
        int finalLimit = Math.min(limit, 50); // Cap at 50 for performance
        
        return topUsers.stream()
                .limit(finalLimit)
                .map(user -> {
                    long totalCompleted = habitLogRepository.countByUser(user);
                    // For simplicity, using total completed. Real streak calculation would be more complex.
                    return new LeaderboardEntryResponse(
                            rank.getAndIncrement(),
                            user.getId(),
                            user.getUsername(),
                            user.getAvatarUrl(),
                            totalCompleted,
                            0 // Placeholder for streak
                    );
                }).toList();
    }
}
