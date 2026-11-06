package com.messwala.backend.dto;

import java.time.LocalDate;

public record CookingCountResponse(
        LocalDate date,
        MealCount lunch,
        MealCount dinner
) {
    public record MealCount(
            int active,
            int paused,
            int skipped,
            int toCook
    ) {
    }
}