package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.event.SubscriptionExpiringEvent;

public interface EmailService {

    void sendExpiryReminder(SubscriptionExpiringEvent event);
}