package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.emuns.Gender;
import com.example.Gym_Management_System.model.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        Long userId,
        String fullName,
        String email,
        String phone,
        LocalDate birthDate,
        Gender gender,
        String emergencyContact,
        boolean enabled,
        LocalDateTime createdAt
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getFullName(),
                member.getUser().getEmail(),
                member.getUser().getPhone(),
                member.getBirthDate(),
                member.getGender(),
                member.getEmergencyContact(),
                member.getUser().isEnabled(),
                member.getCreatedAt()
        );
    }
}