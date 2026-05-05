package com.university.healthysocial.repository;

import com.university.healthysocial.domain.Habit;
import com.university.healthysocial.domain.HabitLog;
import com.university.healthysocial.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HabitLogRepository extends JpaRepository<HabitLog, UUID> {
    List<HabitLog> findAllByHabit(Habit habit);

    Page<HabitLog> findAllByHabitAndLoggedDateBetween(Habit habit, LocalDate start, LocalDate end, Pageable pageable);
    
    boolean existsByHabitAndCreatedAtAfter(Habit habit, OffsetDateTime startOfDay);
    
    long countByHabitAndCreatedAtBetween(Habit habit, OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT COUNT(l) FROM HabitLog l WHERE l.habit.user = :user")
    long countByUser(@Param("user") User user);
}
