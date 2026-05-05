package com.university.healthysocial.repository;

import com.university.healthysocial.domain.Like;
import com.university.healthysocial.domain.Post;
import com.university.healthysocial.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    boolean existsByPostAndUser(Post post, User user);
    Optional<Like> findByPostAndUser(Post post, User user);
    long countByPost(Post post);
}
