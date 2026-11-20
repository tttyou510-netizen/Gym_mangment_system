package com.example.Gym_Management_System.model;

import com.example.Gym_Management_System.emuns.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "members")
public class Member extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String emergencyContact;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Subscription> subscriptions = new ArrayList<>();
}