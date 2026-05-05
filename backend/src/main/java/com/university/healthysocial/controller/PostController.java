package com.university.healthysocial.controller;

import com.university.healthysocial.dto.request.Requests.CreatePostRequest;
import com.university.healthysocial.dto.request.Requests.UpdatePostRequest;
import com.university.healthysocial.dto.response.Responses.PageResponse;
import com.university.healthysocial.dto.response.Responses.PostResponse;
import com.university.healthysocial.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Motivational posts, social feed and likes")
public class PostController {

    private final PostService postService;

    // ── Feed endpoints (public) ───────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Global public feed — newest posts first (no auth required)")
    public ResponseEntity<PageResponse<PostResponse>> getGlobalFeed(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                postService.getGlobalFeed(PageRequest.of(page, size,
                        Sort.by("createdAt").descending())));
    }

    @GetMapping("/feed")
    @Operation(summary = "Personalised feed — posts from users you follow (auth required)")
    public ResponseEntity<PageResponse<PostResponse>> getPersonalisedFeed(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                postService.getPersonalisedFeed(PageRequest.of(page, size,
                        Sort.by("createdAt").descending())));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "All posts by a specific user (public)")
    public ResponseEntity<PageResponse<PostResponse>> getPostsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                postService.getPostsByUser(userId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    // ── Single post ───────────────────────────────────────────────────────────

    @GetMapping("/{postId}")
    @Operation(summary = "Get a single post by ID (public)")
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    // ── Mutations (auth required) ─────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new post")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(req));
    }

    @PutMapping("/{postId}")
    @Operation(summary = "Update a post (only author)")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest req) {
        return ResponseEntity.ok(postService.updatePost(postId, req));
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a post (only author)")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    // ── Likes ─────────────────────────────────────────────────────────────────

    @PostMapping("/{postId}/like")
    @Operation(summary = "Toggle like on a post (like if not liked, unlike if already liked)")
    public ResponseEntity<PostResponse> toggleLike(@PathVariable UUID postId) {
        return ResponseEntity.ok(postService.toggleLike(postId));
    }
}