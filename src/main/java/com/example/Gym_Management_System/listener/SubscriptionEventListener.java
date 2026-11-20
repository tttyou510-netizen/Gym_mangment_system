package com.example.Gym_Management_System.listener;

import com.example.Gym_Management_System.event.SubscriptionExpiringEvent;
import com.example.Gym_Management_System.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SubscriptionEventListener {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionEventListener.class);

    private final EmailService emailService;

    public SubscriptionEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionExpiring(SubscriptionExpiringEvent event) {
        try {
            emailService.sendExpiryReminder(event);
        } catch (Exception ex) {
            log.error("Failed to send expiry reminder for subscription {}",
                    event.subscriptionId(), ex);
        }
    }
}