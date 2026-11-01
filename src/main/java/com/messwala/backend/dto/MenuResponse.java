package com.messwala.backend.dto;

import com.messwala.backend.entity.MealType;

import java.time.LocalDate;

public record MenuResponse(
        Long id,
        LocalDate date,
        MealType mealType,
        String items
) {
}