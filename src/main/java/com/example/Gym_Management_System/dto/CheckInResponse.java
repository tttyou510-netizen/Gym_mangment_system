package com.example.Gym_Management_System.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CheckInResponse(
        Long attendanceId,
        Long memberId,
        String memberName,
        String planName,
        LocalDateTime checkInAt,
        LocalDate subscriptionEndDate,
        long daysRemaining,
        BigDecimal remainingBalance
) {
}