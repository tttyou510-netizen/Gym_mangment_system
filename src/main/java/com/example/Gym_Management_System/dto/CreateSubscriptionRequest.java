package com.example.Gym_Management_System.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateSubscriptionRequest(

        @NotNull(message = "Member id is required")
        Long memberId,

        @NotNull(message = "Plan id is required")
        Long planId,


        @FutureOrPresent(message = "Start date cannot be in the past")
        LocalDate startDate
) {
}