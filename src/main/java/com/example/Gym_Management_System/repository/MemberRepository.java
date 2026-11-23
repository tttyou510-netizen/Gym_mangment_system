package com.example.Gym_Management_System.repository;

import com.example.Gym_Management_System.model.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Member> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query("""
            select m from Member m
            join m.user u
            where lower(u.fullName) like lower(concat('%', :name, '%'))
            """)
    Page<Member> searchByName(@Param("name") String name, Pageable pageable);
    // قفل صف العضو: ضغطتين على Check-in في نفس اللحظة مش هيعملوا جلستين
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findByIdForUpdate(@Param("id") Long id);
}