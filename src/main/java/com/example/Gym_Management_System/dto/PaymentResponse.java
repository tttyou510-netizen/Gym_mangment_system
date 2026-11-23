package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.emuns.PaymentMethod;
import com.example.Gym_Management_System.emuns.PaymentStatus;
import com.example.Gym_Management_System.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long subscriptionId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        LocalDateTime paidAt,
        String receivedBy,
        String note,
        LocalDateTime refundedAt,
        String refundReason
) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getSubscription().getId(),
                p.getAmount(),
                p.getMethod(),
                p.getStatus(),
                p.getPaidAt(),
                p.getReceivedBy().getFullName(),
                p.getNote(),
                p.getRefundedAt(),
                p.getRefundReason()
        );
    }
}