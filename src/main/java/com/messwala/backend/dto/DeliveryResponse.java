package com.messwala.backend.dto;

import com.messwala.backend.entity.DeliveryStatus;
import com.messwala.backend.entity.MealType;

import java.time.LocalDate;

public record DeliveryResponse(
        Long id,
        LocalDate date,
        MealType mealType,
        DeliveryStatus status,
        String customerName,
        String customerPhone,
        String area
) {
}