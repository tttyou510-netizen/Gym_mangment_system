package com.example.Gym_Management_System.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,       // بالثواني
        Long userId,
        String email,
        String fullName,
        String role
) {
}