package com.university.healthysocial.repository;

import com.university.healthysocial.domain.Comment;
import com.university.healthysocial.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    
    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findAllByPost(Post post, Pageable pageable);
    
    long countByPost(Post post);
}
