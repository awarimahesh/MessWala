package com.messwala.backend.dto;

import com.messwala.backend.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BillResponse(
        Long id,
        Long subscriptionId,
        String customerName,
        String planName,
        String month,
        BigDecimal planPrice,
        int skippedDays,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        LocalDate paidOn
) {
}