package com.example.Gym_Management_System.dto;

import java.math.BigDecimal;

public record PaymentSummaryResponse(
        Long subscriptionId,
        String planName,
        BigDecimal price,
        BigDecimal totalPaid,
        BigDecimal remaining,
        boolean fullyPaid
) {
}