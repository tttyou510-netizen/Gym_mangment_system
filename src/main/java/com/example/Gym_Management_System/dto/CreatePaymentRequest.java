package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.emuns.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePaymentRequest(

        @NotNull(message = "Subscription id is required")
        Long subscriptionId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Invalid amount format")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        PaymentMethod method,

        @Size(max = 255, message = "Note must not exceed 255 characters")
        String note
) {
}