package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.common.ResourceNotFoundException;
import com.example.Gym_Management_System.common.exception.DuplicateResourceException;

import com.example.Gym_Management_System.dto.MembershipPlanRequest;
import com.example.Gym_Management_System.dto.MembershipPlanResponse;
import com.example.Gym_Management_System.model.MembershipPlan;
import com.example.Gym_Management_System.repository.MembershipPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MembershipPlanService {

    private final MembershipPlanRepository planRepository;

    public MembershipPlanService(MembershipPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional
    public MembershipPlanResponse create(MembershipPlanRequest request) {
        if (planRepository.existsByName(request.name())) {
            throw new DuplicateResourceException(
                    "Plan already exists with name: " + request.name());
        }

        MembershipPlan plan = new MembershipPlan();
        plan.setName(request.name());
        plan.setDurationInDays(request.durationInDays());
        plan.setPrice(request.price());
        plan.setDescription(request.description());

        return MembershipPlanResponse.from(planRepository.save(plan));
    }

    public MembershipPlanResponse getById(Long id) {
        return MembershipPlanResponse.from(findOrThrow(id));
    }

    public List<MembershipPlanResponse> getAll(boolean onlyActive) {
        List<MembershipPlan> plans = onlyActive
                ? planRepository.findByActiveTrue()
                : planRepository.findAll();

        return plans.stream().map(MembershipPlanResponse::from).toList();
    }

    @Transactional
    public MembershipPlanResponse update(Long id, MembershipPlanRequest request) {
        MembershipPlan plan = findOrThrow(id);

        planRepository.findByName(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Plan already exists with name: " + request.name());
                });

        plan.setName(request.name());
        plan.setDurationInDays(request.durationInDays());
        plan.setPrice(request.price());
        plan.setDescription(request.description());

        return MembershipPlanResponse.from(plan);   // dirty checking بيحفظ التعديل لوحده
    }

    @Transactional
    public void deactivate(Long id) {
        findOrThrow(id).setActive(false);
    }

    private MembershipPlan findOrThrow(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan", id));
    }
}