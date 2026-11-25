package com.example.Gym_Management_System.repository;

import com.example.Gym_Management_System.emuns.ClassStatus;
import com.example.Gym_Management_System.model.ClassSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    // قفل صف الحصة: ده اللي بيحمي "آخر مكان فاضي"
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ClassSession c where c.id = :id")
    Optional<ClassSession> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select c from ClassSession c
            join fetch c.trainer t
            join fetch t.user
            where c.id = :id
            """)
    Optional<ClassSession> findWithTrainerById(@Param("id") Long id);

    @Query("""
            select c from ClassSession c
            join fetch c.trainer t
            join fetch t.user
            where c.startsAt >= :from and c.startsAt < :to
            order by c.startsAt asc
            """)
    List<ClassSession> findInRange(@Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    @Query("""
            select c from ClassSession c
            join fetch c.trainer t
            join fetch t.user
            where c.startsAt >= :from and c.startsAt < :to and t.id = :trainerId
            order by c.startsAt asc
            """)
    List<ClassSession> findInRangeForTrainer(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to,
                                             @Param("trainerId") Long trainerId);

    // حصص المدرب اللي بتتقاطع مع الوقت ده
    @Query("""
            select count(c) from ClassSession c
            where c.trainer.id = :trainerId and c.status = :status
              and c.startsAt < :endsAt and c.endsAt > :startsAt
            """)
    long countTrainerOverlaps(@Param("trainerId") Long trainerId,
                              @Param("status") ClassStatus status,
                              @Param("startsAt") LocalDateTime startsAt,
                              @Param("endsAt") LocalDateTime endsAt);
}