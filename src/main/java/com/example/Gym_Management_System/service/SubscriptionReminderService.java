package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.emuns.SubscriptionStatus;
import com.example.Gym_Management_System.event.SubscriptionExpiringEvent;
import com.example.Gym_Management_System.model.Subscription;
import com.example.Gym_Management_System.repository.SubscriptionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SubscriptionReminderService {

    private final SubscriptionRepository subscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SubscriptionReminderService(SubscriptionRepository subscriptionRepository,
                                       ApplicationEventPublisher eventPublisher) {
        this.subscriptionRepository = subscriptionRepository;
        this.eventPublisher = eventPublisher;
    }
    @Transactional
    public int sendExpiryReminders(int daysBefore) {
        LocalDate today = LocalDate.now();

        List<Subscription> expiring = subscriptionRepository.findExpiringForReminder(
                SubscriptionStatus.ACTIVE, today, today.plusDays(daysBefore));

        int published = 0;
        for (Subscription s : expiring) {
            if (!s.getMember().getUser().isEnabled()) {
                continue;
            }

            // احجز التذكير الأول: لو رجّعت 0 يبقى thread تاني سبقنا
            int claimed = subscriptionRepository.markReminderSent(s.getId(), LocalDateTime.now());
            if (claimed == 0) {
                continue;
            }

            eventPublisher.publishEvent(new SubscriptionExpiringEvent(
                    s.getId(),
                    s.getMember().getUser().getFullName(),
                    s.getMember().getUser().getEmail(),
                    s.getPlan().getName(),
                    s.getEndDate(),
                    ChronoUnit.DAYS.between(today, s.getEndDate())
            ));
            published++;
        }
        return published;
    }
}