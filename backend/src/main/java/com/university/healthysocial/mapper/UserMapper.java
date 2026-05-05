package com.university.healthysocial.mapper;

import com.university.healthysocial.domain.User;
import com.university.healthysocial.dto.response.Responses.UserProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Follower/following counts are computed in the service layer and passed in
 * as separate parameters via the default method below, since MapStruct cannot
 * derive them from lazy-loaded collections without hitting N+1 issues.
 */
@Mapper
public interface UserMapper {

    @Mapping(target = "followersCount", expression = "java(followersCount)")
    @Mapping(target = "followingCount", expression = "java(followingCount)")
    UserProfileResponse toProfileResponse(User user, long followersCount, long followingCount);
}