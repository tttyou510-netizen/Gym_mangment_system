package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.MembershipPlanRequest;
import com.example.Gym_Management_System.dto.MembershipPlanResponse;
import com.example.Gym_Management_System.service.MembershipPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
public class MembershipPlanController {

    private final MembershipPlanService planService;

    public MembershipPlanController(MembershipPlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<MembershipPlanResponse> create(
            @Valid @RequestBody MembershipPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.create(request));
    }

    @GetMapping
    public List<MembershipPlanResponse> getAll(
            @RequestParam(defaultValue = "true") boolean onlyActive) {
        return planService.getAll(onlyActive);
    }

    @GetMapping("/{id}")
    public MembershipPlanResponse getById(@PathVariable Long id) {
        return planService.getById(id);
    }

    @PutMapping("/{id}")
    public MembershipPlanResponse update(@PathVariable Long id,
                                         @Valid @RequestBody MembershipPlanRequest request) {
        return planService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        planService.deactivate(id);
    }
}