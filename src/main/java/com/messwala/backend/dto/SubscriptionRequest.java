package com.messwala.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SubscriptionRequest(
        @NotNull(message = "Plan id is required")
        Long planId,

        LocalDate startDate
) {
}