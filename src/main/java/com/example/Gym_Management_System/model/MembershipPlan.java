package com.example.Gym_Management_System.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "membership_plans")
public class MembershipPlan extends BaseEntity {

    @Column(nullable = false, unique = true)

    private String name;

    @Column(nullable = false)
    private Integer durationInDays;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private String description;

    @Column(nullable = false)
    private boolean active = true;
}