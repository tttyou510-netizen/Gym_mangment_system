package com.example.Gym_Management_System.controller;


import com.example.Gym_Management_System.dto.CreateStaffRequest;
import com.example.Gym_Management_System.dto.PageResponse;
import com.example.Gym_Management_System.dto.StaffResponse;
import com.example.Gym_Management_System.dto.UpdateStaffRequest;
import com.example.Gym_Management_System.emuns.Role;
import com.example.Gym_Management_System.security.SecurityUser;
import com.example.Gym_Management_System.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping
    public ResponseEntity<StaffResponse> create(@Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.create(request));
    }

    @GetMapping
    public PageResponse<StaffResponse> list(@RequestParam(required = false) Role role,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize,
                Sort.by(Sort.Direction.DESC, "id"));
        return staffService.list(role, pageable);
    }

    @GetMapping("/{id}")
    public StaffResponse getById(@PathVariable Long id) {
        return staffService.getById(id);
    }

    @PutMapping("/{id}")
    public StaffResponse update(@PathVariable Long id,
                                @Valid @RequestBody UpdateStaffRequest request) {
        return staffService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public StaffResponse deactivate(@PathVariable Long id,
                                    @AuthenticationPrincipal SecurityUser principal) {
        return staffService.changeStatus(id, false, principal.getId());
    }

    @PatchMapping("/{id}/activate")
    public StaffResponse activate(@PathVariable Long id,
                                  @AuthenticationPrincipal SecurityUser principal) {
        return staffService.changeStatus(id, true, principal.getId());
    }
}