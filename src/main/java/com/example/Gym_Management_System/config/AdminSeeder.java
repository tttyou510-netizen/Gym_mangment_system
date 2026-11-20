package com.example.Gym_Management_System.config;

import com.example.Gym_Management_System.emuns.Role;
import com.example.Gym_Management_System.model.User;
import com.example.Gym_Management_System.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String fullName;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${gym.admin.email:}") String email,
                       @Value("${gym.admin.password:}") String password,
                       @Value("${gym.admin.full-name:Gym Admin}") String fullName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        if (email.isBlank() || password.length() < 8) {
            log.warn("No admin exists and gym.admin.email/password are missing or too short. Skipping seeding.");
            return;
        }

        User admin = new User();
        admin.setFullName(fullName);
        admin.setEmail(email.trim().toLowerCase());
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);

        log.info("Initial admin account created: {}", admin.getEmail());
    }
}