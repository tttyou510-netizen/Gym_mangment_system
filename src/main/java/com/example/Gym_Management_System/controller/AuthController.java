package com.example.Gym_Management_System.controller;

import com.example.Gym_Management_System.dto.AuthResponse;
import com.example.Gym_Management_System.dto.ChangePasswordRequest;
import com.example.Gym_Management_System.dto.CurrentUserResponse;
import com.example.Gym_Management_System.dto.LoginRequest;
import com.example.Gym_Management_System.security.SecurityUser;
import com.example.Gym_Management_System.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal SecurityUser principal) {
        return authService.getCurrentUser(principal.getId());
    }

    @PutMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal SecurityUser principal,
                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request);
    }
}