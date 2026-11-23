package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.common.ResourceNotFoundException;
import com.example.Gym_Management_System.common.exception.BusinessRuleException;
import com.example.Gym_Management_System.dto.CreatePaymentRequest;
import com.example.Gym_Management_System.dto.PaymentResponse;
import com.example.Gym_Management_System.dto.PaymentSummaryResponse;
import com.example.Gym_Management_System.dto.RefundRequest;
import com.example.Gym_Management_System.emuns.PaymentStatus;
import com.example.Gym_Management_System.emuns.SubscriptionStatus;
import com.example.Gym_Management_System.model.Member;
import com.example.Gym_Management_System.model.Payment;
import com.example.Gym_Management_System.model.Subscription;
import com.example.Gym_Management_System.repository.MemberRepository;
import com.example.Gym_Management_System.repository.PaymentRepository;
import com.example.Gym_Management_System.repository.SubscriptionRepository;
import com.example.Gym_Management_System.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          SubscriptionRepository subscriptionRepository,
                          MemberRepository memberRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PaymentResponse create(CreatePaymentRequest request, Long receivedByUserId) {
        // قفل الصف لحد ما الـ transaction تخلص
        Subscription subscription = subscriptionRepository.findByIdForUpdate(request.subscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", request.subscriptionId()));

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot record a payment for a cancelled subscription");
        }

        BigDecimal price = subscription.getPlan().getPrice();
        BigDecimal remaining = price.subtract(totalPaid(subscription.getId()));

        if (remaining.signum() <= 0) {
            throw new BusinessRuleException("Subscription is already fully paid");
        }
        if (request.amount().compareTo(remaining) > 0) {
            throw new BusinessRuleException("Amount exceeds the remaining balance (" + remaining + ")");
        }

        Payment payment = new Payment();
        payment.setSubscription(subscription);
        payment.setAmount(request.amount());
        payment.setMethod(request.method());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        payment.setReceivedBy(userRepository.getReferenceById(receivedByUserId));
        payment.setNote(request.note());

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    public PaymentResponse getById(Long id) {
        return PaymentResponse.from(findOrThrow(id));
    }

    public List<PaymentResponse> getBySubscription(Long subscriptionId) {
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ResourceNotFoundException("Subscription", subscriptionId);
        }
        return paymentRepository.findBySubscriptionIdOrderByPaidAtDesc(subscriptionId)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    public PaymentSummaryResponse getSummary(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findWithDetailsById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", subscriptionId));


        BigDecimal price = subscription.getPlan().getPrice();
        BigDecimal paid = totalPaid(subscriptionId);
        BigDecimal remaining = price.subtract(paid).max(BigDecimal.ZERO);

        return new PaymentSummaryResponse(
                subscriptionId,
                subscription.getPlan().getName(),
                price,
                paid,
                remaining,
                remaining.signum() == 0
        );
    }
    public BigDecimal getRemainingBalance(Subscription subscription) {
        BigDecimal paid = totalPaid(subscription.getId());
        return subscription.getPlan().getPrice().subtract(paid).max(BigDecimal.ZERO);
    }

    // دفعات العضو الحالي (من التوكن)
    public List<PaymentResponse> getByUserId(Long userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile for user", userId));

        return paymentRepository.findBySubscriptionMemberIdOrderByPaidAtDesc(member.getId())
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional
    public PaymentResponse refund(Long id, RefundRequest request) {
        Payment payment = findOrThrow(id);

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Only completed payments can be refunded (current: " + payment.getStatus() + ")");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundReason(request.reason().trim());

        return PaymentResponse.from(payment);
    }

    private BigDecimal totalPaid(Long subscriptionId) {
        BigDecimal total = paymentRepository.sumAmountBySubscriptionIdAndStatus(
                subscriptionId, PaymentStatus.COMPLETED);
        return total == null ? BigDecimal.ZERO : total;
    }

    private Payment findOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }
}