package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.common.ResourceNotFoundException;

import com.example.Gym_Management_System.common.exception.BusinessRuleException;
import com.example.Gym_Management_System.dto.AttendanceResponse;
import com.example.Gym_Management_System.dto.CheckInResponse;
import com.example.Gym_Management_System.dto.PageResponse;
import com.example.Gym_Management_System.emuns.SubscriptionStatus;
import com.example.Gym_Management_System.model.Attendance;
import com.example.Gym_Management_System.model.Member;
import com.example.Gym_Management_System.model.Subscription;
import com.example.Gym_Management_System.repository.AttendanceRepository;
import com.example.Gym_Management_System.repository.MemberRepository;
import com.example.Gym_Management_System.repository.SubscriptionRepository;
import com.example.Gym_Management_System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final boolean requireFullPayment;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             MemberRepository memberRepository,
                             SubscriptionRepository subscriptionRepository,
                             UserRepository userRepository,
                             PaymentService paymentService,
                             @Value("${gym.attendance.require-full-payment:false}") boolean requireFullPayment) {
        this.attendanceRepository = attendanceRepository;
        this.memberRepository = memberRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.requireFullPayment = requireFullPayment;
    }

    @Transactional
    public CheckInResponse checkIn(Long memberId, Long recordedByUserId) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        if (!member.getUser().isEnabled()) {
            throw new BusinessRuleException("Check-in denied: member account is deactivated");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        if (attendanceRepository.existsByMemberIdAndCheckOutAtIsNullAndCheckInAtGreaterThanEqual(
                memberId, today.atStartOfDay())) {
            throw new BusinessRuleException("Member is already checked in");
        }

        Subscription subscription = subscriptionRepository
                .findFirstByMemberIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        memberId, SubscriptionStatus.ACTIVE, today, today)
                .orElseThrow(() -> new BusinessRuleException(
                        "Check-in denied: " + explainNoActiveSubscription(memberId, today)));

        BigDecimal remaining = paymentService.getRemainingBalance(subscription);
        if (requireFullPayment && remaining.signum() > 0) {
            throw new BusinessRuleException("Check-in denied: unpaid balance of " + remaining);
        }

        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setSubscription(subscription);
        attendance.setCheckInAt(now);
        attendance.setRecordedBy(userRepository.getReferenceById(recordedByUserId));
        attendanceRepository.save(attendance);

        return new CheckInResponse(
                attendance.getId(),
                member.getId(),
                member.getUser().getFullName(),
                subscription.getPlan().getName(),
                now,
                subscription.getEndDate(),
                ChronoUnit.DAYS.between(today, subscription.getEndDate()),
                remaining
        );
    }

    @Transactional
    public AttendanceResponse checkOut(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", memberId);
        }

        Attendance attendance = attendanceRepository
                .findFirstByMemberIdAndCheckOutAtIsNullAndCheckInAtGreaterThanEqualOrderByCheckInAtDesc(
                        memberId, LocalDate.now().atStartOfDay())
                .orElseThrow(() -> new BusinessRuleException("Member is not checked in"));

        attendance.setCheckOutAt(LocalDateTime.now());
        return AttendanceResponse.from(attendance);
    }

    public PageResponse<AttendanceResponse> getByDate(LocalDate date, Pageable pageable) {
        LocalDate day = (date != null) ? date : LocalDate.now();
        return PageResponse.from(
                attendanceRepository.findByCheckInAtGreaterThanEqualAndCheckInAtLessThan(
                                day.atStartOfDay(), day.plusDays(1).atStartOfDay(), pageable)
                        .map(AttendanceResponse::from));
    }

    public PageResponse<AttendanceResponse> getByMember(Long memberId, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", memberId);
        }
        return PageResponse.from(
                attendanceRepository.findByMemberId(memberId, pageable).map(AttendanceResponse::from));
    }

    public PageResponse<AttendanceResponse> getByUserId(Long userId, Pageable pageable) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile for user", userId));
        return getByMember(member.getId(), pageable);
    }

    // بيقول للموظف ليه الدخول اتمنع بدل رسالة عامة
    private String explainNoActiveSubscription(Long memberId, LocalDate today) {
        return subscriptionRepository.findAllByMemberIdWithDetails(memberId).stream()
                .findFirst()
                .map(s -> switch (s.getStatus()) {
                    case FROZEN -> "subscription is frozen";
                    case CANCELLED -> "subscription was cancelled";
                    case EXPIRED -> "subscription expired on " + s.getEndDate();
                    case ACTIVE -> s.getStartDate().isAfter(today)
                            ? "subscription starts on " + s.getStartDate()
                            : "subscription ended on " + s.getEndDate();
                })
                .orElse("member has no subscription");
    }
}