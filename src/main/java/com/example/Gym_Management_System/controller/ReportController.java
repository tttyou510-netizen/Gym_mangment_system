package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.PlanPopularityResponse;
import com.example.Gym_Management_System.dto.RevenueReportResponse;
import com.example.Gym_Management_System.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // الافتراضي: من أول الشهر لحد النهارده
    @GetMapping("/revenue")
    public RevenueReportResponse revenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate end = (to != null) ? to : LocalDate.now();
        LocalDate start = (from != null) ? from : end.withDayOfMonth(1);
        return reportService.revenue(start, end);
    }

    @GetMapping("/plans/popularity")
    public List<PlanPopularityResponse> planPopularity() {
        return reportService.planPopularity();
    }
}