package com.example.Gym_Management_System.dto;

import jakarta.validation.constraints.NotNull;

public record BookClassRequest(@NotNull(message = "Class id is required")
                               Long classId) {
}
