package com.example.Gym_Management_System.repository;

import com.example.Gym_Management_System.emuns.Role;
import com.example.Gym_Management_System.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository  extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);
    Page<User> findByRoleIn(Collection<Role> roles, Pageable pageable);
    boolean existsByRole(Role role);

}

