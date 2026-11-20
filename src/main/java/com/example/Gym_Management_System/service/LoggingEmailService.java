package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.event.SubscriptionExpiringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendExpiryReminder(SubscriptionExpiringEvent event) {
        log.info("""
                [EMAIL] To: {}
                Hi {}, your '{}' subscription ends on {} ({} day(s) left). Renew to keep training!""",
                event.memberEmail(), event.memberName(), event.planName(),
                event.endDate(), event.daysLeft());
    }
}