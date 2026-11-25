package com.example.Gym_Management_System.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTrainerRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 150, message = "Full name must not exceed 150 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @Pattern(regexp = "^[0-9+]{7,15}$", message = "Invalid phone number")
        String phone,

        @NotBlank(message = "Specialty is required")
        @Size(max = 100, message = "Specialty must not exceed 100 characters")
        String specialty,

        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio
) {
}