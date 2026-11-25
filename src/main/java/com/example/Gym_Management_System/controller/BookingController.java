package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.BookingResponse;
import com.example.Gym_Management_System.dto.StaffBookingRequest;
import com.example.Gym_Management_System.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody StaffBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.book(request.classId(), request.memberId()));
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancelByStaff(id);
    }

    @GetMapping
    public List<BookingResponse> byMember(@RequestParam Long memberId) {
        return bookingService.getByMember(memberId);
    }
}