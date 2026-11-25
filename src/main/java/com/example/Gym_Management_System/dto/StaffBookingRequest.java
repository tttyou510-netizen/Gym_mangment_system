package com.example.Gym_Management_System.dto;

import jakarta.validation.constraints.NotNull;

public record StaffBookingRequest(

        @NotNull(message = "Class id is required")
        Long classId,

        @NotNull(message = "Member id is required")
        Long memberId
) {
}