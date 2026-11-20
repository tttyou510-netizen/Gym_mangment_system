package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.model.MembershipPlan;

import java.math.BigDecimal;

public record MembershipPlanResponse(
        Long id,
        String name,
        Integer durationInDays,
        BigDecimal price,
        String description,
        boolean active
) {

    public static MembershipPlanResponse from(MembershipPlan plan) {
        return new MembershipPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getDurationInDays(),
                plan.getPrice(),
                plan.getDescription(),
                plan.isActive()
        );
    }
}