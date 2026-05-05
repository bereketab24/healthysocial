package com.university.healthysocial.repository;

import com.university.healthysocial.domain.Goal;
import com.university.healthysocial.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {
    Page<Goal> findAllByUser(User user, Pageable pageable);
    List<Goal> findAllByUserAndIsCompleted(User user, boolean isCompleted);
}
