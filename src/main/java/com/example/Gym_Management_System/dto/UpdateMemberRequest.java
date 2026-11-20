package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.emuns.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateMemberRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 150, message = "Full name must not exceed 150 characters")
        String fullName,

        @Pattern(regexp = "^[0-9+]{7,15}$", message = "Invalid phone number")
        String phone,

        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @NotNull(message = "Gender is required")
        Gender gender,

        @Size(max = 255, message = "Emergency contact must not exceed 255 characters")
        String emergencyContact
) {
}