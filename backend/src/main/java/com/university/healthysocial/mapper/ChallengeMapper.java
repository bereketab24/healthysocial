package com.university.healthysocial.mapper;

import com.university.healthysocial.domain.Challenge;
import com.university.healthysocial.dto.response.Responses.ChallengeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(imports = LocalDate.class)
public interface ChallengeMapper {

    @Mapping(target = "creatorId",       source = "challenge.creator.id")
    @Mapping(target = "creatorUsername", source = "challenge.creator.username")
    @Mapping(target = "participantsCount", expression = "java(participantsCount)")
    @Mapping(target = "isJoined",        expression = "java(isJoined)")
    @Mapping(target = "isActive",        expression = "java(!LocalDate.now().isBefore(challenge.getStartDate()) && !LocalDate.now().isAfter(challenge.getEndDate()))")
    ChallengeResponse toResponse(Challenge challenge, long participantsCount, boolean isJoined);
}