package com.example.Gym_Management_System.dto;

public record CurrentUserResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        String role,
        Long memberId      // بيبقى null لو المستخدم مش عضو (أدمن مثلًا)
) {
}