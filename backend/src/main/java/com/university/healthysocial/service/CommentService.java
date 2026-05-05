package com.university.healthysocial.service;

import com.university.healthysocial.domain.Comment;
import com.university.healthysocial.domain.Post;
import com.university.healthysocial.domain.User;
import com.university.healthysocial.dto.request.Requests.CreateCommentRequest;
import com.university.healthysocial.dto.response.Responses.CommentResponse;
import com.university.healthysocial.dto.response.Responses.PageResponse;
import com.university.healthysocial.exception.ForbiddenOperationException;
import com.university.healthysocial.exception.ResourceNotFoundException;
import com.university.healthysocial.mapper.CommentMapper;
import com.university.healthysocial.repository.CommentRepository;
import com.university.healthysocial.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final CommentMapper commentMapper;

    public PageResponse<CommentResponse> getComments(UUID postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        Page<Comment> comments = commentRepository.findAllByPost(post, pageable);
        
        return new PageResponse<>(
                comments.getContent().stream().map(commentMapper::toResponse).toList(),
                comments.getNumber(),
                comments.getSize(),
                comments.getTotalElements(),
                comments.getTotalPages(),
                comments.isLast()
        );
    }

    @Transactional
    public CommentResponse addComment(UUID postId, CreateCommentRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        User currentUser = userService.resolveCurrentUser();
        
        Comment comment = Comment.builder()
                .post(post)
                .user(currentUser)
                .content(req.content())
                .build();
        
        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        
        User currentUser = userService.resolveCurrentUser();
        // Author of comment or author of post can delete comment
        if (!comment.getUser().getId().equals(currentUser.getId()) && 
            !comment.getPost().getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You are not authorized to delete this comment");
        }
        
        commentRepository.delete(comment);
    }
}
