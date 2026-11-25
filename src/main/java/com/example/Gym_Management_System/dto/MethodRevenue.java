package com.example.Gym_Management_System.dto;

import com.example.Gym_Management_System.emuns.PaymentMethod;

import java.math.BigDecimal;

public record MethodRevenue(PaymentMethod method, BigDecimal amount) {
}