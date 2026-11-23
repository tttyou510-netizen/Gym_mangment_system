package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.emuns.Role;
import com.example.Gym_Management_System.model.User;

import java.time.LocalDateTime;

public record StaffResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        Role role,
        boolean enabled,
        LocalDateTime createdAt
) {

    public static StaffResponse from(User user) {
        return new StaffResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}