package com.university.healthysocial.repository;

import com.university.healthysocial.domain.Follow;
import com.university.healthysocial.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {
    boolean existsByFollowerAndFollowing(User follower, User following);
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    
    Page<Follow> findAllByFollower(User follower, Pageable pageable);
    Page<Follow> findAllByFollowing(User following, Pageable pageable);

    long countByFollower(User follower);
    long countByFollowing(User following);
}
