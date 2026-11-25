package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.emuns.BookingStatus;
import com.example.Gym_Management_System.model.Booking;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long classId,
        String classTitle,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        Long memberId,
        String memberName,
        BookingStatus status,
        LocalDateTime bookedAt,
        LocalDateTime cancelledAt
) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getClassSession().getId(),
                b.getClassSession().getTitle(),
                b.getClassSession().getStartsAt(),
                b.getClassSession().getEndsAt(),
                b.getMember().getId(),
                b.getMember().getUser().getFullName(),
                b.getStatus(),
                b.getBookedAt(),
                b.getCancelledAt()
        );
    }
}