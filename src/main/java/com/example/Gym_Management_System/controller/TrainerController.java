package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.CreateTrainerRequest;
import com.example.Gym_Management_System.dto.TrainerResponse;
import com.example.Gym_Management_System.dto.UpdateTrainerRequest;
import com.example.Gym_Management_System.service.TrainerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trainers")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @PostMapping
    public ResponseEntity<TrainerResponse> create(@Valid @RequestBody CreateTrainerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerService.create(request));
    }

    @GetMapping
    public List<TrainerResponse> getAll() {
        return trainerService.getAll();
    }

    @GetMapping("/{id}")
    public TrainerResponse getById(@PathVariable Long id) {
        return trainerService.getById(id);
    }

    @PutMapping("/{id}")
    public TrainerResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateTrainerRequest request) {
        return trainerService.update(id, request);
    }
}