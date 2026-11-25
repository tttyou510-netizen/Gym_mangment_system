package com.example.Gym_Management_System.repository;

import com.example.Gym_Management_System.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUserId(Long userId);

    @Query("select t from Trainer t join fetch t.user order by t.id")
    List<Trainer> findAllWithUser();
}