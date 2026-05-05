package com.university.healthysocial.repository;

import com.university.healthysocial.domain.Challenge;
import com.university.healthysocial.domain.enums.HabitCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {
    
    @EntityGraph(attributePaths = {"creator"})
    Page<Challenge> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = {"creator"})
    Page<Challenge> findAllByCategory(HabitCategory category, Pageable pageable);

    @Query("SELECT c FROM Challenge c WHERE (:category IS NULL OR c.category = :category) AND (:activeOnly = false OR (c.startDate <= :today AND c.endDate >= :today))")
    @EntityGraph(attributePaths = {"creator"})
    Page<Challenge> findChallenges(@Param("category") HabitCategory category, @Param("activeOnly") boolean activeOnly, @Param("today") LocalDate today, Pageable pageable);
}
