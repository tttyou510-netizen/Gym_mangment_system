package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.CreateMemberRequest;
import com.example.Gym_Management_System.dto.MemberResponse;
import com.example.Gym_Management_System.dto.PageResponse;
import com.example.Gym_Management_System.dto.UpdateMemberRequest;
import com.example.Gym_Management_System.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody CreateMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(request));
    }

    @GetMapping
    public PageResponse<MemberResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));

        return memberService.search(name, pageable);
    }

    @GetMapping("/{id}")
    public MemberResponse getById(@PathVariable Long id) {
        return memberService.getById(id);
    }

    @PutMapping("/{id}")
    public MemberResponse update(@PathVariable Long id,
                                 @Valid @RequestBody UpdateMemberRequest request) {
        return memberService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        memberService.deactivate(id);
    }
}