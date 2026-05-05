package com.university.healthysocial.repository;

import com.university.healthysocial.domain.Post;
import com.university.healthysocial.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    
    @EntityGraph(attributePaths = {"user"})
    Page<Post> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Post> findAllByUser(User user, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user IN (SELECT f.following FROM Follow f WHERE f.follower = :user)")
    @EntityGraph(attributePaths = {"user"})
    Page<Post> findPersonalisedFeed(@Param("user") User user, Pageable pageable);

    long countByUser(User user);
}
