package com.university.healthysocial.service;

import com.university.healthysocial.domain.Like;
import com.university.healthysocial.domain.Post;
import com.university.healthysocial.domain.User;
import com.university.healthysocial.dto.request.Requests.CreatePostRequest;
import com.university.healthysocial.dto.request.Requests.UpdatePostRequest;
import com.university.healthysocial.dto.response.Responses.PageResponse;
import com.university.healthysocial.dto.response.Responses.PostResponse;
import com.university.healthysocial.exception.ForbiddenOperationException;
import com.university.healthysocial.exception.ResourceNotFoundException;
import com.university.healthysocial.mapper.PostMapper;
import com.university.healthysocial.repository.CommentRepository;
import com.university.healthysocial.repository.LikeRepository;
import com.university.healthysocial.repository.PostRepository;
import com.university.healthysocial.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final PostMapper postMapper;
    private final SecurityUtils securityUtils;

    public PageResponse<PostResponse> getGlobalFeed(Pageable pageable) {
        Page<Post> posts = postRepository.findAllBy(pageable);
        return toPageResponse(posts);
    }

    public PageResponse<PostResponse> getPersonalisedFeed(Pageable pageable) {
        User currentUser = userService.resolveCurrentUser();
        Page<Post> posts = postRepository.findPersonalisedFeed(currentUser, pageable);
        return toPageResponse(posts);
    }

    public PageResponse<PostResponse> getPostsByUser(UUID userId, Pageable pageable) {
        User user = userService.findById(userId);
        Page<Post> posts = postRepository.findAllByUser(user, pageable);
        return toPageResponse(posts);
    }

    public PostResponse getPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        return mapToResponse(post);
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest req) {
        User currentUser = userService.resolveCurrentUser();
        Post post = Post.builder()
                .user(currentUser)
                .content(req.content())
                .imageUrl(req.imageUrl())
                .postType(req.postType())
                .build();
        return mapToResponse(postRepository.save(post));
    }

    @Transactional
    public PostResponse updatePost(UUID postId, UpdatePostRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        
        User currentUser = userService.resolveCurrentUser();
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only update your own posts");
        }
        
        post.setContent(req.content());
        post.setImageUrl(req.imageUrl());
        
        return mapToResponse(postRepository.save(post));
    }

    @Transactional
    public void deletePost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        
        User currentUser = userService.resolveCurrentUser();
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only delete your own posts");
        }
        
        postRepository.delete(post);
    }

    @Transactional
    public PostResponse toggleLike(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
        User currentUser = userService.resolveCurrentUser();
        
        Optional<Like> existingLike = likeRepository.findByPostAndUser(post, currentUser);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        } else {
            likeRepository.save(Like.builder().post(post).user(currentUser).build());
        }
        
        return mapToResponse(post);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private PostResponse mapToResponse(Post post) {
        long likesCount = likeRepository.countByPost(post);
        long commentsCount = commentRepository.countByPost(post);
        boolean likedByCurrentUser = false;
        
        if (securityUtils.isAuthenticated()) {
            try {
                User currentUser = userService.resolveCurrentUser();
                likedByCurrentUser = likeRepository.existsByPostAndUser(post, currentUser);
            } catch (Exception ignored) {}
        }
        
        return postMapper.toResponse(post, likesCount, commentsCount, likedByCurrentUser);
    }

    private PageResponse<PostResponse> toPageResponse(Page<Post> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::mapToResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
