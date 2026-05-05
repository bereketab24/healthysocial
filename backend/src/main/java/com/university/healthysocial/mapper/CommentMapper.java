package com.university.healthysocial.mapper;

import com.university.healthysocial.domain.Comment;
import com.university.healthysocial.dto.response.Responses.CommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CommentMapper {

    @Mapping(target = "postId",          source = "post.id")
    @Mapping(target = "authorId",        source = "user.id")
    @Mapping(target = "authorUsername",  source = "user.username")
    @Mapping(target = "authorAvatarUrl", source = "user.avatarUrl")
    CommentResponse toResponse(Comment comment);
}