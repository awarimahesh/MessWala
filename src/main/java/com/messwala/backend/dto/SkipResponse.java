package com.messwala.backend.dto;

import com.messwala.backend.entity.MealType;

import java.time.LocalDate;

public record SkipResponse(
        Long id,
        Long subscriptionId,
        LocalDate date,
        MealType mealType
) {
}