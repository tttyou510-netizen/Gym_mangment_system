package com.example.Gym_Management_System.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateStaffRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 150, message = "Full name must not exceed 150 characters")
        String fullName,

        @Pattern(regexp = "^[0-9+]{7,15}$", message = "Invalid phone number")
        String phone
) {
}