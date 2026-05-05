package com.university.healthysocial.service;

import com.university.healthysocial.domain.Challenge;
import com.university.healthysocial.domain.ChallengeParticipant;
import com.university.healthysocial.domain.User;
import com.university.healthysocial.domain.enums.HabitCategory;
import com.university.healthysocial.dto.request.Requests.CreateChallengeRequest;
import com.university.healthysocial.dto.response.Responses.ChallengeResponse;
import com.university.healthysocial.dto.response.Responses.PageResponse;
import com.university.healthysocial.exception.DuplicateResourceException;
import com.university.healthysocial.exception.ForbiddenOperationException;
import com.university.healthysocial.exception.ResourceNotFoundException;
import com.university.healthysocial.mapper.ChallengeMapper;
import com.university.healthysocial.repository.ChallengeParticipantRepository;
import com.university.healthysocial.repository.ChallengeRepository;
import com.university.healthysocial.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository participantRepository;
    private final UserService userService;
    private final ChallengeMapper challengeMapper;
    private final SecurityUtils securityUtils;

    public PageResponse<ChallengeResponse> getAllChallenges(HabitCategory category, boolean activeOnly, Pageable pageable) {
        Page<Challenge> challenges = challengeRepository.findChallenges(category, activeOnly, LocalDate.now(), pageable);
        
        return new PageResponse<>(
                challenges.getContent().stream().map(this::mapToResponse).toList(),
                challenges.getNumber(),
                challenges.getSize(),
                challenges.getTotalElements(),
                challenges.getTotalPages(),
                challenges.isLast()
        );
    }

    public ChallengeResponse getChallenge(UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", challengeId));
        return mapToResponse(challenge);
    }

    @Transactional
    public ChallengeResponse createChallenge(CreateChallengeRequest req) {
        User creator = userService.resolveCurrentUser();
        Challenge challenge = Challenge.builder()
                .creator(creator)
                .title(req.title())
                .description(req.description())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .category(req.category())
                .build();
        
        Challenge saved = challengeRepository.save(challenge);
        
        // Auto-join creator
        joinChallenge(saved.getId());
        
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteChallenge(UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", challengeId));
        User user = userService.resolveCurrentUser();
        
        if (!challenge.getCreator().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Only the creator can delete the challenge");
        }
        
        challengeRepository.delete(challenge);
    }

    @Transactional
    public ChallengeResponse joinChallenge(UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", challengeId));
        User user = userService.resolveCurrentUser();
        
        if (participantRepository.existsByChallengeAndUser(challenge, user)) {
            // If already joined, just return the response
            return mapToResponse(challenge);
        }
        
        ChallengeParticipant participant = ChallengeParticipant.builder()
                .challenge(challenge)
                .user(user)
                .build();
        
        participantRepository.save(participant);
        return mapToResponse(challenge);
    }

    @Transactional
    public ChallengeResponse leaveChallenge(UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", challengeId));
        User user = userService.resolveCurrentUser();
        
        participantRepository.findByChallengeAndUser(challenge, user)
                .ifPresent(participantRepository::delete);
        
        return mapToResponse(challenge);
    }

    @Transactional
    public ChallengeResponse markCompleted(UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", challengeId));
        User user = userService.resolveCurrentUser();
        
        ChallengeParticipant participant = participantRepository.findByChallengeAndUser(challenge, user)
                .orElseThrow(() -> new ForbiddenOperationException("You must join the challenge first"));
        
        participant.setCompleted(true);
        participantRepository.save(participant);
        
        return mapToResponse(challenge);
    }

    private ChallengeResponse mapToResponse(Challenge challenge) {
        long participantsCount = participantRepository.countByChallenge(challenge);
        boolean isJoined = false;
        
        if (securityUtils.isAuthenticated()) {
            try {
                User currentUser = userService.resolveCurrentUser();
                isJoined = participantRepository.existsByChallengeAndUser(challenge, currentUser);
            } catch (Exception ignored) {}
        }
        
        return challengeMapper.toResponse(challenge, participantsCount, isJoined);
    }
}
