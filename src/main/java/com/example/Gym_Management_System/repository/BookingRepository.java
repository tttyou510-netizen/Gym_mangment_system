package com.example.Gym_Management_System.repository;

import com.example.Gym_Management_System.dto.ClassBookingCount;
import com.example.Gym_Management_System.emuns.BookingStatus;
import com.example.Gym_Management_System.model.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByClassSessionIdAndMemberId(Long classId, Long memberId);

    long countByClassSessionIdAndStatus(Long classId, BookingStatus status);

    // عدد المحجوزين لكل حصة في query واحد (بدل query لكل حصة)
    @Query("""
            select new com.example.Gym_Management_System.dto.ClassBookingCount(b.classSession.id, count(b))
            from Booking b
            where b.classSession.id in :ids and b.status = :status
            group by b.classSession.id
            """)
    List<ClassBookingCount> countByClassIds(@Param("ids") Collection<Long> ids,
                                            @Param("status") BookingStatus status);

    // حجوزات العضو اللي بتتقاطع مع وقت الحصة (غير الحصة نفسها)
    @Query("""
            select count(b) from Booking b
            where b.member.id = :memberId and b.status = :status
              and b.classSession.id <> :classId
              and b.classSession.startsAt < :endsAt and b.classSession.endsAt > :startsAt
            """)
    long countMemberOverlaps(@Param("memberId") Long memberId,
                             @Param("status") BookingStatus status,
                             @Param("classId") Long classId,
                             @Param("startsAt") LocalDateTime startsAt,
                             @Param("endsAt") LocalDateTime endsAt);

    @EntityGraph(attributePaths = {"member.user"})
    List<Booking> findByClassSessionIdAndStatusOrderByBookedAtAsc(Long classId, BookingStatus status);

    @EntityGraph(attributePaths = {"classSession", "member.user"})
    List<Booking> findByMemberIdOrderByClassSessionStartsAtDesc(Long memberId);
}