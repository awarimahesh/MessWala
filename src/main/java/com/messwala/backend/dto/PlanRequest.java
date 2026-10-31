package com.messwala.backend.dto;

import com.messwala.backend.entity.MealType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PlanRequest(
        @NotBlank(message = "Plan name is required")
        @Size(max = 100, message = "Plan name must be at most 100 characters")
        String name,

        @NotNull(message = "Meal type is required")
        MealType mealType,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "1.00", message = "Price must be at least 1")
        BigDecimal pricePerMonth
) {
}