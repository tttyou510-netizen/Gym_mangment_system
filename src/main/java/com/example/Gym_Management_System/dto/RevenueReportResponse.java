package com.example.Gym_Management_System.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RevenueReportResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalRevenue,
        BigDecimal refundedTotal,
        long paymentsCount,
        List<DailyRevenue> daily,
        List<MethodRevenue> byMethod
) {
}