package com.university.healthysocial.mapper;

import com.university.healthysocial.domain.Goal;
import com.university.healthysocial.dto.response.Responses.GoalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface GoalMapper {
    @Mapping(target = "isCompleted", source = "goal.completed")
    GoalResponse toResponse(Goal goal);
}
