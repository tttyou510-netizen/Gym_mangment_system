package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.CreateSubscriptionRequest;
import com.example.Gym_Management_System.dto.SubscriptionResponse;
import com.example.Gym_Management_System.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.subscribe(request));
    }

    @GetMapping("/{id}")
    public SubscriptionResponse getById(@PathVariable Long id) {
        return subscriptionService.getById(id);
    }

    @GetMapping
    public List<SubscriptionResponse> getByMember(@RequestParam Long memberId) {
        return subscriptionService.getByMember(memberId);
    }

    @PatchMapping("/{id}/cancel")
    public SubscriptionResponse cancel(@PathVariable Long id) {
        return subscriptionService.cancel(id);
    }

    @PatchMapping("/{id}/freeze")
    public SubscriptionResponse freeze(@PathVariable Long id) {
        return subscriptionService.freeze(id);
    }

    @PatchMapping("/{id}/unfreeze")
    public SubscriptionResponse unfreeze(@PathVariable Long id) {
        return subscriptionService.unfreeze(id);
    }
}