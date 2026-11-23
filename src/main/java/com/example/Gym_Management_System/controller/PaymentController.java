package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.CreatePaymentRequest;
import com.example.Gym_Management_System.dto.PaymentResponse;
import com.example.Gym_Management_System.dto.PaymentSummaryResponse;
import com.example.Gym_Management_System.dto.RefundRequest;
import com.example.Gym_Management_System.security.SecurityUser;
import com.example.Gym_Management_System.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request,
                                                  @AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.create(request, principal.getId()));
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable Long id) {
        return paymentService.getById(id);
    }

    @GetMapping
    public List<PaymentResponse> getBySubscription(@RequestParam Long subscriptionId) {
        return paymentService.getBySubscription(subscriptionId);
    }

    @GetMapping("/summary")
    public PaymentSummaryResponse summary(@RequestParam Long subscriptionId) {
        return paymentService.getSummary(subscriptionId);
    }

    @PostMapping("/{id}/refund")
    public PaymentResponse refund(@PathVariable Long id,
                                  @Valid @RequestBody RefundRequest request) {
        return paymentService.refund(id, request);
    }
}