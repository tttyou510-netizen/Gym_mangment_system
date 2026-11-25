package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.DashboardSummaryResponse;
import com.example.Gym_Management_System.dto.ExpiringSubscriptionResponse;
import com.example.Gym_Management_System.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardSummaryResponse summary(@RequestParam(defaultValue = "7") int expiringDays) {
        return dashboardService.getSummary(expiringDays);
    }

    @GetMapping("/expiring")
    public List<ExpiringSubscriptionResponse> expiring(@RequestParam(defaultValue = "7") int days) {
        return dashboardService.getExpiring(days);
    }
}