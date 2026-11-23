package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.PageResponse;
import com.example.Gym_Management_System.dto.AttendanceResponse;
import com.example.Gym_Management_System.dto.MemberResponse;
import com.example.Gym_Management_System.dto.PaymentResponse;
import com.example.Gym_Management_System.dto.SubscriptionResponse;
import com.example.Gym_Management_System.security.SecurityUser;
import com.example.Gym_Management_System.service.AttendanceService;
import com.example.Gym_Management_System.service.MemberService;
import com.example.Gym_Management_System.service.PaymentService;
import com.example.Gym_Management_System.service.SubscriptionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;
    private final AttendanceService attendanceService;

    public MeController(MemberService memberService,
                        SubscriptionService subscriptionService,
                        PaymentService paymentService,
                        AttendanceService attendanceService) {
        this.memberService = memberService;
        this.subscriptionService = subscriptionService;
        this.paymentService = paymentService;
        this.attendanceService = attendanceService;
    }

    @GetMapping("/profile")
    public MemberResponse profile(@AuthenticationPrincipal SecurityUser principal) {
        return memberService.getByUserId(principal.getId());
    }

    @GetMapping("/subscriptions")
    public List<SubscriptionResponse> subscriptions(@AuthenticationPrincipal SecurityUser principal) {
        return subscriptionService.getByUserId(principal.getId());
    }

    @GetMapping("/payments")
    public List<PaymentResponse> payments(@AuthenticationPrincipal SecurityUser principal) {
        return paymentService.getByUserId(principal.getId());
    }

    @GetMapping("/attendance")
    public PageResponse<AttendanceResponse> attendance(@AuthenticationPrincipal SecurityUser principal,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return attendanceService.getByUserId(principal.getId(),
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, "checkInAt")));
    }
}