package com.university.healthysocial.repository;

import com.university.healthysocial.domain.Habit;
import com.university.healthysocial.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HabitRepository extends JpaRepository<Habit, UUID> {
    Page<Habit> findAllByUser(User user, Pageable pageable);
    Page<Habit> findAllByUserAndIsActive(User user, boolean isActive, Pageable pageable);
}
