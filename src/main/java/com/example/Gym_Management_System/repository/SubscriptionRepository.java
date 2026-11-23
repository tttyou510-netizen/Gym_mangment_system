package com.example.Gym_Management_System.repository;

import com.example.Gym_Management_System.emuns.SubscriptionStatus;
import com.example.Gym_Management_System.model.Subscription;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByMemberId(Long memberId);

    boolean existsByMemberIdAndStatus(Long memberId, SubscriptionStatus status);
    // اشتراك ACTIVE ولسه ما انتهاش (بيحل مشكلة اشتراك انتهى بس الـ Scheduler لسه ما حدّثش حالته)
    boolean existsByMemberIdAndStatusAndEndDateGreaterThanEqual(Long memberId,
                                                                SubscriptionStatus status,
                                                                LocalDate date);
    // للـ Scheduler: الاشتراكات اللي لسه ACTIVE وتاريخها عدّى
    List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDate date);

    // لتذكير العضو: اشتراكات هتخلص خلال فترة معينة
    List<Subscription> findByStatusAndEndDateBetween(SubscriptionStatus status,
                                                     LocalDate from,
                                                     LocalDate to);

    // JOIN FETCH عشان نجيب العضو والخطة في query واحد (نتجنب N+1)
    @Query("""
            select s from Subscription s
            join fetch s.member m
            join fetch m.user
            join fetch s.plan
            where s.id = :id
            """)
    Optional<Subscription> findWithDetailsById(@Param("id") Long id);

    @Query("""
            select s from Subscription s
            join fetch s.member m
            join fetch m.user
            join fetch s.plan
            where m.id = :memberId
            order by s.startDate desc
            """)
    List<Subscription> findAllByMemberIdWithDetails(@Param("memberId") Long memberId);

    @Query("""
        select s from Subscription s
        join fetch s.member m
        join fetch m.user
        join fetch s.plan
        where s.status = :status
          and s.endDate between :from and :to
          and s.reminderSentAt is null
        """)
    List<Subscription> findExpiringForReminder(@Param("status") SubscriptionStatus status,
                                               @Param("from") LocalDate from,
                                               @Param("to") LocalDate to);
    @Modifying
    @Query("""
        update Subscription s
        set s.reminderSentAt = :now
        where s.id = :id and s.reminderSentAt is null
        """)
    int markReminderSent(@Param("id") Long id, @Param("now") LocalDateTime now);
    // قفل الصف أثناء تسجيل الدفع: دفعتين في نفس اللحظة مش هيعدّوا الفحص مع بعض
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.id = :id")
    Optional<Subscription> findByIdForUpdate(@Param("id") Long id);
    // الاشتراك السارِي النهارده: ACTIVE وبدأ ولسه ما انتهاش
    @EntityGraph(attributePaths = "plan")
    Optional<Subscription> findFirstByMemberIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long memberId, SubscriptionStatus status, LocalDate startDate, LocalDate endDate);
}