package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.emuns.ClassStatus;
import com.example.Gym_Management_System.model.ClassSession;

import java.time.LocalDateTime;

public record ClassResponse(
        Long id,
        String title,
        String description,
        Long trainerId,
        String trainerName,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        int capacity,
        long bookedCount,
        long availableSpots,
        ClassStatus status
) {

    public static ClassResponse from(ClassSession c, long bookedCount) {
        return new ClassResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getTrainer().getId(),
                c.getTrainer().getUser().getFullName(),
                c.getStartsAt(),
                c.getEndsAt(),
                c.getCapacity(),
                bookedCount,
                Math.max(c.getCapacity() - bookedCount, 0),
                c.getStatus()
        );
    }
}