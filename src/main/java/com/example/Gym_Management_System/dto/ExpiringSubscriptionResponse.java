package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.model.Subscription;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record ExpiringSubscriptionResponse(
        Long subscriptionId,
        Long memberId,
        String memberName,
        String phone,
        String planName,
        LocalDate endDate,
        long daysLeft
) {

    public static ExpiringSubscriptionResponse from(Subscription s, LocalDate today) {
        return new ExpiringSubscriptionResponse(
                s.getId(),
                s.getMember().getId(),
                s.getMember().getUser().getFullName(),
                s.getMember().getUser().getPhone(),
                s.getPlan().getName(),
                s.getEndDate(),
                ChronoUnit.DAYS.between(today, s.getEndDate())
        );
    }
}