package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.common.ResourceNotFoundException;
import com.example.Gym_Management_System.common.exception.BadRequestException;
import com.example.Gym_Management_System.common.exception.DuplicateResourceException;
import com.example.Gym_Management_System.dto.CreateStaffRequest;
import com.example.Gym_Management_System.dto.PageResponse;
import com.example.Gym_Management_System.dto.StaffResponse;
import com.example.Gym_Management_System.dto.UpdateStaffRequest;
import com.example.Gym_Management_System.emuns.Role;
import com.example.Gym_Management_System.model.User;
import com.example.Gym_Management_System.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StaffService {

    private static final List<Role> STAFF_ROLES =
            List.of(Role.ADMIN, Role.RECEPTIONIST, Role.TRAINER);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public StaffResponse create(CreateStaffRequest request) {
        if (request.role() == Role.MEMBER) {
            throw new BadRequestException("Members must be registered through the members endpoint");
        }

        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already in use: " + email);
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setEnabled(true);

        return StaffResponse.from(userRepository.save(user));
    }

    public PageResponse<StaffResponse> list(Role role, Pageable pageable) {
        if (role == Role.MEMBER) {
            throw new BadRequestException("Role must be one of ADMIN, RECEPTIONIST, TRAINER");
        }

        Page<User> page = (role == null)
                ? userRepository.findByRoleIn(STAFF_ROLES, pageable)
                : userRepository.findByRole(role, pageable);

        return PageResponse.from(page.map(StaffResponse::from));
    }

    public StaffResponse getById(Long id) {
        return StaffResponse.from(findStaffOrThrow(id));
    }

    @Transactional
    public StaffResponse update(Long id, UpdateStaffRequest request) {
        User user = findStaffOrThrow(id);
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        return StaffResponse.from(user);
    }

    @Transactional
    public StaffResponse changeStatus(Long id, boolean enabled, Long currentUserId) {
        User user = findStaffOrThrow(id);

        if (!enabled && user.getId().equals(currentUserId)) {
            throw new BadRequestException("You cannot deactivate your own account");
        }

        user.setEnabled(enabled);
        return StaffResponse.from(user);
    }

    // أي مستخدم دوره MEMBER بنعتبره مش موجود هنا
    private User findStaffOrThrow(Long id) {
        return userRepository.findById(id)
                .filter(u -> u.getRole() != Role.MEMBER)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
    }
}