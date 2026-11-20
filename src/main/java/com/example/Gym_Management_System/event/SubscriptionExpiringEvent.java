package com.example.Gym_Management_System.event;

import java.time.LocalDate;

public record SubscriptionExpiringEvent(
        Long subscriptionId,
        String memberName,
        String memberEmail,
        String planName,
        LocalDate endDate,
        long daysLeft
) {
}