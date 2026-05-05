package com.university.healthysocial.mapper;

import com.university.healthysocial.domain.Post;
import com.university.healthysocial.dto.response.Responses.PostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PostMapper {

    @Mapping(target = "authorId",        source = "post.user.id")
    @Mapping(target = "authorUsername",  source = "post.user.username")
    @Mapping(target = "authorAvatarUrl", source = "post.user.avatarUrl")
    @Mapping(target = "likesCount",      expression = "java(likesCount)")
    @Mapping(target = "commentsCount",   expression = "java(commentsCount)")
    @Mapping(target = "likedByCurrentUser", expression = "java(likedByCurrentUser)")
    PostResponse toResponse(Post post,
                            long likesCount,
                            long commentsCount,
                            boolean likedByCurrentUser);
}