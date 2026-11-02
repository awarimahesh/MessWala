package com.messwala.backend.dto;

import com.messwala.backend.entity.MealType;
import com.messwala.backend.entity.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionResponse(
        Long id,
        Long planId,
        String planName,
        MealType mealType,
        BigDecimal pricePerMonth,
        LocalDate startDate,
        LocalDate endDate,
        SubscriptionStatus status
) {
}