package com.example.Gym_Management_System.repository;

import com.example.Gym_Management_System.model.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {

    Optional<MembershipPlan> findByName(String name);

    boolean existsByName(String name);

    List<MembershipPlan> findByActiveTrue();
}