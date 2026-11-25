package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.BookClassRequest;
import com.example.Gym_Management_System.dto.BookingResponse;
import com.example.Gym_Management_System.security.SecurityUser;
import com.example.Gym_Management_System.service.BookingService;
import com.example.Gym_Management_System.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/bookings")
public class MeBookingController {

    private final BookingService bookingService;
    private final MemberService memberService;

    public MeBookingController(BookingService bookingService, MemberService memberService) {
        this.bookingService = bookingService;
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody BookClassRequest request,
                                                @AuthenticationPrincipal SecurityUser principal) {
        Long memberId = memberService.getByUserId(principal.getId()).id();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.book(request.classId(), memberId));
    }

    @GetMapping
    public List<BookingResponse> mine(@AuthenticationPrincipal SecurityUser principal) {
        return bookingService.getByUserId(principal.getId());
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id,
                                  @AuthenticationPrincipal SecurityUser principal) {
        return bookingService.cancelByMember(id, principal.getId());
    }
}