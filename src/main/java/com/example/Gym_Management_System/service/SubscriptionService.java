package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.common.ResourceNotFoundException;
import com.example.Gym_Management_System.common.exception.BusinessRuleException;

import com.example.Gym_Management_System.dto.CreateSubscriptionRequest;
import com.example.Gym_Management_System.dto.SubscriptionResponse;
import com.example.Gym_Management_System.emuns.SubscriptionStatus;
import com.example.Gym_Management_System.model.Member;
import com.example.Gym_Management_System.model.MembershipPlan;
import com.example.Gym_Management_System.model.Subscription;
import com.example.Gym_Management_System.repository.MemberRepository;
import com.example.Gym_Management_System.repository.MembershipPlanRepository;
import com.example.Gym_Management_System.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               MemberRepository memberRepository,
                               MembershipPlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
    }

    @Transactional
    public SubscriptionResponse subscribe(CreateSubscriptionRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.memberId()));

        MembershipPlan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan", request.planId()));

        if (!member.getUser().isEnabled()) {
            throw new BusinessRuleException("Member account is deactivated");
        }
        if (!plan.isActive()) {
            throw new BusinessRuleException("Membership plan is not active");
        }

        LocalDate today = LocalDate.now();
        boolean hasLiveSubscription =
                subscriptionRepository.existsByMemberIdAndStatusAndEndDateGreaterThanEqual(
                        member.getId(), SubscriptionStatus.ACTIVE, today)
                        || subscriptionRepository.existsByMemberIdAndStatus(
                        member.getId(), SubscriptionStatus.FROZEN);

        if (hasLiveSubscription) {
            throw new BusinessRuleException("Member already has an active or frozen subscription");
        }

        LocalDate startDate = request.startDate() != null ? request.startDate() : today;

        Subscription subscription = new Subscription();
        subscription.setMember(member);
        subscription.setPlan(plan);
        subscription.setStartDate(startDate);
        subscription.setEndDate(startDate.plusDays(plan.getDurationInDays()));
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        return SubscriptionResponse.from(subscriptionRepository.save(subscription));
    }

    public SubscriptionResponse getById(Long id) {
        return SubscriptionResponse.from(findOrThrow(id));
    }

    public List<SubscriptionResponse> getByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", memberId);
        }
        return subscriptionRepository.findAllByMemberIdWithDetails(memberId)
                .stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionResponse cancel(Long id) {
        Subscription s = findOrThrow(id);

        if (s.getStatus() != SubscriptionStatus.ACTIVE && s.getStatus() != SubscriptionStatus.FROZEN) {
            throw new BusinessRuleException(
                    "Only active or frozen subscriptions can be cancelled (current: " + s.getStatus() + ")");
        }

        s.setStatus(SubscriptionStatus.CANCELLED);
        s.setFrozenAt(null);
        return SubscriptionResponse.from(s);
    }

    @Transactional
    public SubscriptionResponse freeze(Long id) {
        Subscription s = findOrThrow(id);

        if (s.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Only active subscriptions can be frozen (current: " + s.getStatus() + ")");
        }
        if (s.getEndDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Subscription has already ended");
        }

        s.setStatus(SubscriptionStatus.FROZEN);
        s.setFrozenAt(LocalDate.now());
        return SubscriptionResponse.from(s);
    }

    @Transactional
    public SubscriptionResponse unfreeze(Long id) {
        Subscription s = findOrThrow(id);

        if (s.getStatus() != SubscriptionStatus.FROZEN) {
            throw new BusinessRuleException(
                    "Only frozen subscriptions can be unfrozen (current: " + s.getStatus() + ")");
        }

        long frozenDays = ChronoUnit.DAYS.between(s.getFrozenAt(), LocalDate.now());
        s.setEndDate(s.getEndDate().plusDays(frozenDays));
        s.setFrozenAt(null);
       s.setReminderSentAt(null);
        s.setStatus(SubscriptionStatus.ACTIVE);
        return SubscriptionResponse.from(s);
    }

    private Subscription findOrThrow(Long id) {
        return subscriptionRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", id));
    }
    @Transactional
    public int expireOverdueSubscriptions() {
        List<Subscription> overdue = subscriptionRepository
                .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, LocalDate.now());

        overdue.forEach(s -> s.setStatus(SubscriptionStatus.EXPIRED));

        return overdue.size();
    }
}