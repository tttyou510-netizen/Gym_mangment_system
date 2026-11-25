package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.BookingResponse;
import com.example.Gym_Management_System.dto.ClassResponse;
import com.example.Gym_Management_System.dto.CreateClassRequest;
import com.example.Gym_Management_System.security.SecurityUser;
import com.example.Gym_Management_System.service.ClassService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @PostMapping
    public ResponseEntity<ClassResponse> create(@Valid @RequestBody CreateClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.create(request));
    }

    // الافتراضي: من النهارده لـ 7 أيام. ?from=2026-11-25&to=2026-12-05&trainerId=1
    @GetMapping
    public List<ClassResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long trainerId) {

        LocalDate start = (from != null) ? from : LocalDate.now();
        LocalDate end = (to != null) ? to : start.plusDays(6);
        return classService.list(start, end, trainerId);
    }

    // حصص المدرب الحالي
    @GetMapping("/mine")
    public List<ClassResponse> mine(
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate start = (from != null) ? from : LocalDate.now();
        LocalDate end = (to != null) ? to : start.plusDays(6);
        return classService.listForTrainerUser(principal.getId(), start, end);
    }

    @GetMapping("/{id}")
    public ClassResponse getById(@PathVariable Long id) {
        return classService.getById(id);
    }

    @PatchMapping("/{id}/cancel")
    public ClassResponse cancel(@PathVariable Long id) {
        return classService.cancel(id);
    }

    @GetMapping("/{id}/bookings")
    public List<BookingResponse> roster(@PathVariable Long id,
                                        @AuthenticationPrincipal SecurityUser principal) {
        return classService.getRoster(id, principal);
    }
}