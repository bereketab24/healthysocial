package com.university.healthysocial.mapper;

import com.university.healthysocial.domain.Habit;
import com.university.healthysocial.domain.HabitLog;
import com.university.healthysocial.dto.response.Responses.HabitLogResponse;
import com.university.healthysocial.dto.response.Responses.HabitResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface HabitMapper {

    @Mapping(target = "currentStreak", expression = "java(currentStreak)")
    @Mapping(target = "longestStreak", expression = "java(longestStreak)")
    @Mapping(target = "isActive",      source = "habit.active")
    HabitResponse toResponse(Habit habit, int currentStreak, int longestStreak);

    @Mapping(target = "habitId",   source = "log.habit.id")
    @Mapping(target = "habitName", source = "log.habit.name")
    HabitLogResponse toLogResponse(HabitLog log);
}
