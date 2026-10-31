package com.messwala.backend.dto;

import com.messwala.backend.entity.MealType;

import java.math.BigDecimal;

public record PlanResponse(
        Long id,
        String name,
        MealType mealType,
        BigDecimal pricePerMonth,
        boolean active
) {
}
