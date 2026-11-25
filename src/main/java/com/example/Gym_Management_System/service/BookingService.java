package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.common.ResourceNotFoundException;
import com.example.Gym_Management_System.common.exception.BusinessRuleException;
import com.example.Gym_Management_System.dto.BookingResponse;
import com.example.Gym_Management_System.emuns.BookingStatus;
import com.example.Gym_Management_System.emuns.ClassStatus;
import com.example.Gym_Management_System.emuns.SubscriptionStatus;
import com.example.Gym_Management_System.model.Booking;
import com.example.Gym_Management_System.model.ClassSession;
import com.example.Gym_Management_System.model.Member;
import com.example.Gym_Management_System.repository.BookingRepository;
import com.example.Gym_Management_System.repository.ClassSessionRepository;
import com.example.Gym_Management_System.repository.MemberRepository;
import com.example.Gym_Management_System.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ClassSessionRepository classRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final long cancellationHoursBefore;

    public BookingService(BookingRepository bookingRepository,
                          ClassSessionRepository classRepository,
                          MemberRepository memberRepository,
                          SubscriptionRepository subscriptionRepository,
                          @Value("${gym.classes.cancellation-hours-before:2}") long cancellationHoursBefore) {
        this.bookingRepository = bookingRepository;
        this.classRepository = classRepository;
        this.memberRepository = memberRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.cancellationHoursBefore = cancellationHoursBefore;
    }

    @Transactional
    public BookingResponse book(Long classId, Long memberId) {
        // القفل دايمًا بالترتيب ده: الحصة الأول ثم العضو (عشان ما يحصلش deadlock)
        ClassSession session = classRepository.findByIdForUpdate(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", classId));

        if (session.getStatus() != ClassStatus.SCHEDULED) {
            throw new BusinessRuleException("Booking denied: class is cancelled");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!session.getStartsAt().isAfter(now)) {
            throw new BusinessRuleException("Booking denied: class has already started");
        }

        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        if (!member.getUser().isEnabled()) {
            throw new BusinessRuleException("Booking denied: member account is deactivated");
        }

        // الاشتراك لازم يغطي يوم الحصة (مش النهارده)
        LocalDate classDate = session.getStartsAt().toLocalDate();
        subscriptionRepository
                .findFirstByMemberIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        memberId, SubscriptionStatus.ACTIVE, classDate, classDate)
                .orElseThrow(() -> new BusinessRuleException(
                        "Booking denied: no active subscription covering " + classDate));

        Booking existing = bookingRepository.findByClassSessionIdAndMemberId(classId, memberId)
                .orElse(null);
        if (existing != null && existing.getStatus() == BookingStatus.BOOKED) {
            throw new BusinessRuleException("Member has already booked this class");
        }

        if (bookingRepository.countMemberOverlaps(memberId, BookingStatus.BOOKED, classId,
                session.getStartsAt(), session.getEndsAt()) > 0) {
            throw new BusinessRuleException("Member has another booking at the same time");
        }

        long booked = bookingRepository.countByClassSessionIdAndStatus(classId, BookingStatus.BOOKED);
        if (booked >= session.getCapacity()) {
            throw new BusinessRuleException("Class is full");
        }

        // لو العضو لغى قبل كده، بنرجّع نفس الصف (القيد الفريد بيمنع صف تاني)
        Booking booking = (existing != null) ? existing : new Booking();
        if (existing == null) {
            booking.setClassSession(session);
            booking.setMember(member);
        }
        booking.setStatus(BookingStatus.BOOKED);
        booking.setBookedAt(now);
        booking.setCancelledAt(null);

        return BookingResponse.from(bookingRepository.save(booking));
    }

    // العضو بيلغي حجزه هو (بشرط نافذة الإلغاء)
    @Transactional
    public BookingResponse cancelByMember(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getMember().getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        requireActive(booking);

        LocalDateTime deadline = booking.getClassSession().getStartsAt().minusHours(cancellationHoursBefore);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new BusinessRuleException("Cancellation window closed: bookings can be cancelled up to "
                    + cancellationHoursBefore + " hour(s) before the class starts");
        }

        return cancel(booking);
    }

    // الاستقبال أو الأدمن: من غير نافذة
    @Transactional
    public BookingResponse cancelByStaff(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        requireActive(booking);
        return cancel(booking);
    }

    public List<BookingResponse> getByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", memberId);
        }
        return bookingRepository.findByMemberIdOrderByClassSessionStartsAtDesc(memberId)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    public List<BookingResponse> getByUserId(Long userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile for user", userId));
        return getByMember(member.getId());
    }

    private void requireActive(Booking booking) {
        if (booking.getStatus() != BookingStatus.BOOKED) {
            throw new BusinessRuleException("Booking is already cancelled");
        }
    }

    private BookingResponse cancel(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        return BookingResponse.from(booking);
    }
}