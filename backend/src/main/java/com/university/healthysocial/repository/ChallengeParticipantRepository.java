package com.university.healthysocial.repository;

import com.university.healthysocial.domain.Challenge;
import com.university.healthysocial.domain.ChallengeParticipant;
import com.university.healthysocial.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, UUID> {
    boolean existsByChallengeAndUser(Challenge challenge, User user);
    Optional<ChallengeParticipant> findByChallengeAndUser(Challenge challenge, User user);
    long countByChallenge(Challenge challenge);
}
