package com.example.Gym_Management_System.repository;

import com.example.Gym_Management_System.emuns.PaymentStatus;
import com.example.Gym_Management_System.model.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = "receivedBy")
    List<Payment> findBySubscriptionIdOrderByPaidAtDesc(Long subscriptionId);

    @EntityGraph(attributePaths = "receivedBy")
    List<Payment> findBySubscriptionMemberIdOrderByPaidAtDesc(Long memberId);

    // بترجّع null لو مفيش دفعات (بنحوّلها لصفر في الـ Service)
    @Query("""
            select sum(p.amount) from Payment p
            where p.subscription.id = :subscriptionId and p.status = :status
            """)
    BigDecimal sumAmountBySubscriptionIdAndStatus(@Param("subscriptionId") Long subscriptionId,
                                                  @Param("status") PaymentStatus status);
}