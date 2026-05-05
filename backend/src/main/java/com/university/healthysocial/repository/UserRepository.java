package com.university.healthysocial.repository;

import com.university.healthysocial.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByKeycloakId(String keycloakId);
    Optional<User> findByUsername(String username);
    boolean existsByKeycloakId(String keycloakId);
    boolean existsByUsername(String username);
    @Query("SELECT u FROM User u LEFT JOIN u.habits h LEFT JOIN h.logs l GROUP BY u.id ORDER BY COUNT(l) DESC")
    List<User> findLeaderboard();
}