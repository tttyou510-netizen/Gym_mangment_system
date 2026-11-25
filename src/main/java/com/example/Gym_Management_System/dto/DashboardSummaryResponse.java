package com.example.Gym_Management_System.dto;

public record DashboardSummaryResponse(
        long totalMembers,
        long activeSubscriptions,
        long expiringSoon,
        int expiringDays,
        long todayCheckIns,
        long currentlyInside,
        long newMembersThisMonth
) {
}