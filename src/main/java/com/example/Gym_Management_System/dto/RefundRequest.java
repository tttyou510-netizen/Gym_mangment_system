package com.example.Gym_Management_System.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefundRequest(

        @NotBlank(message = "Refund reason is required")
        @Size(max = 255, message = "Reason must not exceed 255 characters")
        String reason
) {
}