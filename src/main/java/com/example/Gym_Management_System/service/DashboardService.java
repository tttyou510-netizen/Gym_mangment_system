package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.dto.DashboardSummaryResponse;
import com.example.Gym_Management_System.dto.ExpiringSubscriptionResponse;
import com.example.Gym_Management_System.emuns.SubscriptionStatus;
import com.example.Gym_Management_System.repository.AttendanceRepository;
import com.example.Gym_Management_System.repository.MemberRepository;
import com.example.Gym_Management_System.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AttendanceRepository attendanceRepository;

    public DashboardService(MemberRepository memberRepository,
                            SubscriptionRepository subscriptionRepository,
                            AttendanceRepository attendanceRepository) {
        this.memberRepository = memberRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public DashboardSummaryResponse getSummary(int expiringDays) {
        int days = clampDays(expiringDays);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();

        return new DashboardSummaryResponse(
                memberRepository.countByUserEnabledTrue(),
                subscriptionRepository.countByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        SubscriptionStatus.ACTIVE, today, today),
                subscriptionRepository.countByStatusAndEndDateBetween(
                        SubscriptionStatus.ACTIVE, today, today.plusDays(days)),
                days,
                attendanceRepository.countByCheckInAtGreaterThanEqualAndCheckInAtLessThan(
                        startOfToday, startOfTomorrow),
                attendanceRepository.countByCheckOutAtIsNullAndCheckInAtGreaterThanEqual(startOfToday),
                memberRepository.countByCreatedAtGreaterThanEqual(startOfMonth)
        );
    }

    public List<ExpiringSubscriptionResponse> getExpiring(int days) {
        LocalDate today = LocalDate.now();
        return subscriptionRepository
                .findExpiringWithDetails(SubscriptionStatus.ACTIVE, today, today.plusDays(clampDays(days)))
                .stream()
                .map(s -> ExpiringSubscriptionResponse.from(s, today))
                .toList();
    }

    private int clampDays(int days) {
        return Math.min(Math.max(days, 1), 90);
    }
}