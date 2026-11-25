package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.common.ResourceNotFoundException;
import com.example.Gym_Management_System.common.exception.DuplicateResourceException;
import com.example.Gym_Management_System.dto.CreateTrainerRequest;
import com.example.Gym_Management_System.dto.TrainerResponse;
import com.example.Gym_Management_System.dto.UpdateTrainerRequest;
import com.example.Gym_Management_System.emuns.Role;
import com.example.Gym_Management_System.model.Trainer;
import com.example.Gym_Management_System.model.User;
import com.example.Gym_Management_System.repository.TrainerRepository;
import com.example.Gym_Management_System.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TrainerService(TrainerRepository trainerRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TrainerResponse create(CreateTrainerRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already in use: " + email);
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(Role.TRAINER);
        user.setEnabled(true);
        userRepository.save(user);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialty(request.specialty().trim());
        trainer.setBio(request.bio());

        return TrainerResponse.from(trainerRepository.save(trainer));
    }

    public List<TrainerResponse> getAll() {
        return trainerRepository.findAllWithUser().stream().map(TrainerResponse::from).toList();
    }

    public TrainerResponse getById(Long id) {
        return TrainerResponse.from(findOrThrow(id));
    }

    @Transactional
    public TrainerResponse update(Long id, UpdateTrainerRequest request) {
        Trainer trainer = findOrThrow(id);
        trainer.getUser().setFullName(request.fullName().trim());
        trainer.getUser().setPhone(request.phone());
        trainer.setSpecialty(request.specialty().trim());
        trainer.setBio(request.bio());
        return TrainerResponse.from(trainer);
    }

    private Trainer findOrThrow(Long id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", id));
    }
}