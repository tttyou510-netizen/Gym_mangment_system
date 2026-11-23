package com.example.Gym_Management_System.dto;

import jakarta.validation.constraints.NotNull;

public record AttendanceRequest(

        @NotNull(message = "Member id is required")
        Long memberId
) {
}