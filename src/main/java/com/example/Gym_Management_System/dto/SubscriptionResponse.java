package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.emuns.SubscriptionStatus;
import com.example.Gym_Management_System.model.Subscription;

import java.time.LocalDate;

public record SubscriptionResponse(
        Long id,
        Long memberId,
        String memberName,
        Long planId,
        String planName,
        LocalDate startDate,
        LocalDate endDate,
        SubscriptionStatus status
) {

    public static SubscriptionResponse from(Subscription s) {
        return new SubscriptionResponse(
                s.getId(),
                s.getMember().getId(),
                s.getMember().getUser().getFullName(),
                s.getPlan().getId(),
                s.getPlan().getName(),
                s.getStartDate(),
                s.getEndDate(),
                s.getStatus()
        );
    }
}