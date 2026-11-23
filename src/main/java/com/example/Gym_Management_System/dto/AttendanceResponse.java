package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.model.Attendance;

import java.time.Duration;
import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        Long memberId,
        String memberName,
        String planName,
        LocalDateTime checkInAt,
        LocalDateTime checkOutAt,
        Long durationMinutes
) {

    public static AttendanceResponse from(Attendance a) {
        Long minutes = a.getCheckOutAt() == null
                ? null
                : Duration.between(a.getCheckInAt(), a.getCheckOutAt()).toMinutes();

        return new AttendanceResponse(
                a.getId(),
                a.getMember().getId(),
                a.getMember().getUser().getFullName(),
                a.getSubscription().getPlan().getName(),
                a.getCheckInAt(),
                a.getCheckOutAt(),
                minutes
        );
    }
}