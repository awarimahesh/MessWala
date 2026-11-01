package com.messwala.backend.dto;

import com.messwala.backend.entity.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MenuRequest(
        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Meal type is required")
        MealType mealType,

        @NotBlank(message = "Items are required")
        @Size(max = 500, message = "Items must be at most 500 characters")
        String items
) {
}