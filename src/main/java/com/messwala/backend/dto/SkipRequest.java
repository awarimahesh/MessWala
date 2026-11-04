package com.messwala.backend.dto;

import com.messwala.backend.entity.MealType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SkipRequest(
        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Meal type is required")
        MealType mealType
) {
}