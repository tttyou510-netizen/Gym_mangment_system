package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.common.exception.BadRequestException;
import com.example.Gym_Management_System.dto.DailyRevenue;
import com.example.Gym_Management_System.dto.MethodRevenue;
import com.example.Gym_Management_System.dto.PlanPopularityResponse;
import com.example.Gym_Management_System.dto.RevenueReportResponse;
import com.example.Gym_Management_System.emuns.PaymentMethod;
import com.example.Gym_Management_System.emuns.PaymentStatus;
import com.example.Gym_Management_System.model.Payment;
import com.example.Gym_Management_System.repository.PaymentRepository;
import com.example.Gym_Management_System.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final long MAX_RANGE_DAYS = 366;

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    public ReportService(PaymentRepository paymentRepository,
                         SubscriptionRepository subscriptionRepository) {
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public RevenueReportResponse revenue(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' must not be after 'to'");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
            throw new BadRequestException("Date range must not exceed " + MAX_RANGE_DAYS + " days");
        }

        List<Payment> payments = paymentRepository.findByPaidAtGreaterThanEqualAndPaidAtLessThan(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        List<Payment> completed = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .toList();

        BigDecimal total = sum(completed);
        BigDecimal refunded = sum(payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .toList());

        Map<LocalDate, BigDecimal> byDay = completed.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPaidAt().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));

        // كل الأيام في الفترة (اللي مفيهاش دفعات بصفر) عشان الرسم البياني
        List<DailyRevenue> daily = from.datesUntil(to.plusDays(1))
                .map(d -> new DailyRevenue(d, byDay.getOrDefault(d, BigDecimal.ZERO)))
                .toList();

        Map<PaymentMethod, BigDecimal> byMethodMap = completed.stream()
                .collect(Collectors.groupingBy(
                        Payment::getMethod,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));

        List<MethodRevenue> byMethod = byMethodMap.entrySet().stream()
                .map(e -> new MethodRevenue(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(MethodRevenue::amount).reversed())
                .toList();

        return new RevenueReportResponse(from, to, total, refunded, completed.size(), daily, byMethod);
    }

    public List<PlanPopularityResponse> planPopularity() {
        return subscriptionRepository.findPlanPopularity();
    }

    private BigDecimal sum(List<Payment> payments) {
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}