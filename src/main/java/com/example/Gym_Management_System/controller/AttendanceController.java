package com.example.Gym_Management_System.controller;


import com.example.Gym_Management_System.dto.AttendanceRequest;
import com.example.Gym_Management_System.dto.AttendanceResponse;
import com.example.Gym_Management_System.dto.CheckInResponse;
import com.example.Gym_Management_System.dto.PageResponse;
import com.example.Gym_Management_System.security.SecurityUser;
import com.example.Gym_Management_System.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<CheckInResponse> checkIn(@Valid @RequestBody AttendanceRequest request,
                                                   @AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.checkIn(request.memberId(), principal.getId()));
    }

    @PostMapping("/check-out")
    public AttendanceResponse checkOut(@Valid @RequestBody AttendanceRequest request) {
        return attendanceService.checkOut(request.memberId());
    }

    // حضور يوم معين (الافتراضي النهارده): ?date=2026-11-23
    @GetMapping
    public PageResponse<AttendanceResponse> byDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return attendanceService.getByDate(date, pageable(page, size));
    }

    @GetMapping("/member/{memberId}")
    public PageResponse<AttendanceResponse> byMember(@PathVariable Long memberId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return attendanceService.getByMember(memberId, pageable(page, size));
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "checkInAt"));
    }
}