package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.model.Trainer;

public record TrainerResponse(
        Long id,
        Long userId,
        String fullName,
        String email,
        String phone,
        String specialty,
        String bio,
        boolean enabled
) {

    public static TrainerResponse from(Trainer t) {
        return new TrainerResponse(
                t.getId(),
                t.getUser().getId(),
                t.getUser().getFullName(),
                t.getUser().getEmail(),
                t.getUser().getPhone(),
                t.getSpecialty(),
                t.getBio(),
                t.getUser().isEnabled()
        );
    }
}