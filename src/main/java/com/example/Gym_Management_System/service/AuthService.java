package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.common.ResourceNotFoundException;
import com.example.Gym_Management_System.common.exception.BadRequestException;

import com.example.Gym_Management_System.dto.AuthResponse;
import com.example.Gym_Management_System.dto.ChangePasswordRequest;
import com.example.Gym_Management_System.dto.CurrentUserResponse;
import com.example.Gym_Management_System.dto.LoginRequest;
import com.example.Gym_Management_System.model.Member;
import com.example.Gym_Management_System.model.User;
import com.example.Gym_Management_System.repository.MemberRepository;
import com.example.Gym_Management_System.repository.UserRepository;
import com.example.Gym_Management_System.security.JwtService;
import com.example.Gym_Management_System.security.SecurityUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserRepository userRepository,
                       MemberRepository memberRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));

        SecurityUser user = (SecurityUser) authentication.getPrincipal();

        return new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getExpiresInSeconds(),
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole()
        );
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(Long userId) {
        User user = findUserOrThrow(userId);
        Long memberId = memberRepository.findByUserId(userId).map(Member::getId).orElse(null);

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name(),
                memberId
        );
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUserOrThrow(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}