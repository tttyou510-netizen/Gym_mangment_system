package com.example.Gym_Management_System.scheduler;

import com.example.Gym_Management_System.service.SubscriptionReminderService;
import com.example.Gym_Management_System.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionScheduler.class);

    private final SubscriptionService subscriptionService;
    private final SubscriptionReminderService reminderService;
    private final int reminderDaysBefore;

    public SubscriptionScheduler(SubscriptionService subscriptionService,
                                 SubscriptionReminderService reminderService,
                                 @Value("${gym.reminder.days-before:3}") int reminderDaysBefore) {
        this.subscriptionService = subscriptionService;
        this.reminderService = reminderService;
        this.reminderDaysBefore = reminderDaysBefore;
    }

    // كل يوم 1 صباحًا
    @Scheduled(cron = "${gym.scheduler.expire-cron:0 0 1 * * *}", zone = "Africa/Cairo")
    public void expireSubscriptions() {
        try {
            int count = subscriptionService.expireOverdueSubscriptions();
            log.info("Subscription expiry job finished: {} subscription(s) expired", count);
        } catch (Exception ex) {
            log.error("Subscription expiry job failed", ex);
        }
    }

    // كل يوم 9 صباحًا
    @Scheduled(cron = "${gym.scheduler.reminder-cron:0 0 9 * * *}", zone = "Africa/Cairo")
    public void sendExpiryReminders() {
        try {
            int count = reminderService.sendExpiryReminders(reminderDaysBefore);
            log.info("Expiry reminder job finished: {} reminder(s) published", count);
        } catch (Exception ex) {
            log.error("Expiry reminder job failed", ex);
        }
    }

    // تعويض لو التطبيق كان مقفول وقت الـ cron (الاتنين idempotent)
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        expireSubscriptions();
        sendExpiryReminders();
    }
}