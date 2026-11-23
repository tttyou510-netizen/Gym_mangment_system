package com.example.Gym_Management_System.repository;

import com.example.Gym_Management_System.model.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // جلسة مفتوحة النهارده (دخل وما خرجش)
    boolean existsByMemberIdAndCheckOutAtIsNullAndCheckInAtGreaterThanEqual(Long memberId,
                                                                            LocalDateTime from);

    Optional<Attendance> findFirstByMemberIdAndCheckOutAtIsNullAndCheckInAtGreaterThanEqualOrderByCheckInAtDesc(
            Long memberId, LocalDateTime from);

    @EntityGraph(attributePaths = {"member.user", "subscription.plan"})
    Page<Attendance> findByCheckInAtGreaterThanEqualAndCheckInAtLessThan(LocalDateTime from,
                                                                         LocalDateTime to,
                                                                         Pageable pageable);

    @EntityGraph(attributePaths = {"member.user", "subscription.plan"})
    Page<Attendance> findByMemberId(Long memberId, Pageable pageable);
}