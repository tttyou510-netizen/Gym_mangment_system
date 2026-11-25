package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.common.ResourceNotFoundException;
import com.example.Gym_Management_System.common.exception.BadRequestException;
import com.example.Gym_Management_System.common.exception.BusinessRuleException;
import com.example.Gym_Management_System.dto.BookingResponse;
import com.example.Gym_Management_System.dto.ClassBookingCount;
import com.example.Gym_Management_System.dto.ClassResponse;
import com.example.Gym_Management_System.dto.CreateClassRequest;
import com.example.Gym_Management_System.emuns.BookingStatus;
import com.example.Gym_Management_System.emuns.ClassStatus;
import com.example.Gym_Management_System.model.ClassSession;
import com.example.Gym_Management_System.model.Trainer;
import com.example.Gym_Management_System.repository.BookingRepository;
import com.example.Gym_Management_System.repository.ClassSessionRepository;
import com.example.Gym_Management_System.repository.TrainerRepository;
import com.example.Gym_Management_System.security.SecurityUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ClassService {

    private static final long MAX_RANGE_DAYS = 60;

    private final ClassSessionRepository classRepository;
    private final TrainerRepository trainerRepository;
    private final BookingRepository bookingRepository;

    public ClassService(ClassSessionRepository classRepository,
                        TrainerRepository trainerRepository,
                        BookingRepository bookingRepository) {
        this.classRepository = classRepository;
        this.trainerRepository = trainerRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public ClassResponse create(CreateClassRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new BadRequestException("End time must be after start time");
        }

        Trainer trainer = trainerRepository.findById(request.trainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", request.trainerId()));

        if (!trainer.getUser().isEnabled()) {
            throw new BusinessRuleException("Trainer account is deactivated");
        }
        if (classRepository.countTrainerOverlaps(trainer.getId(), ClassStatus.SCHEDULED,
                request.startsAt(), request.endsAt()) > 0) {
            throw new BusinessRuleException("Trainer already has a class during this time");
        }

        ClassSession session = new ClassSession();
        session.setTitle(request.title().trim());
        session.setDescription(request.description());
        session.setTrainer(trainer);
        session.setStartsAt(request.startsAt());
        session.setEndsAt(request.endsAt());
        session.setCapacity(request.capacity());
        session.setStatus(ClassStatus.SCHEDULED);

        return ClassResponse.from(classRepository.save(session), 0);
    }

    public List<ClassResponse> list(LocalDate from, LocalDate to, Long trainerId) {
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' must not be after 'to'");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
            throw new BadRequestException("Date range must not exceed " + MAX_RANGE_DAYS + " days");
        }

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<ClassSession> sessions = (trainerId == null)
                ? classRepository.findInRange(start, end)
                : classRepository.findInRangeForTrainer(start, end, trainerId);

        Map<Long, Long> counts = bookedCounts(sessions);
        return sessions.stream()
                .map(c -> ClassResponse.from(c, counts.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    // حصص المدرب الحالي (من التوكن)
    public List<ClassResponse> listForTrainerUser(Long userId, LocalDate from, LocalDate to) {
        Trainer trainer = trainerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer profile for user", userId));
        return list(from, to, trainer.getId());
    }

    public ClassResponse getById(Long id) {
        ClassSession session = classRepository.findWithTrainerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", id));
        long booked = bookingRepository.countByClassSessionIdAndStatus(id, BookingStatus.BOOKED);
        return ClassResponse.from(session, booked);
    }

    @Transactional
    public ClassResponse cancel(Long id) {
        ClassSession session = classRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", id));

        if (session.getStatus() != ClassStatus.SCHEDULED) {
            throw new BusinessRuleException("Class is already cancelled");
        }

        session.setStatus(ClassStatus.CANCELLED);

        // إلغاء كل الحجوزات النشطة
        LocalDateTime now = LocalDateTime.now();
        bookingRepository.findByClassSessionIdAndStatusOrderByBookedAtAsc(id, BookingStatus.BOOKED)
                .forEach(b -> {
                    b.setStatus(BookingStatus.CANCELLED);
                    b.setCancelledAt(now);
                });

        return ClassResponse.from(session, 0);
    }

    // قايمة المحجوزين: المدرب يشوف حصصه هو بس
    public List<BookingResponse> getRoster(Long classId, SecurityUser principal) {
        ClassSession session = classRepository.findWithTrainerById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", classId));

        if ("TRAINER".equals(principal.getRole())
                && !session.getTrainer().getUser().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You can only view the roster of your own classes");
        }

        return bookingRepository.findByClassSessionIdAndStatusOrderByBookedAtAsc(classId, BookingStatus.BOOKED)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    private Map<Long, Long> bookedCounts(List<ClassSession> sessions) {
        if (sessions.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = sessions.stream().map(ClassSession::getId).toList();
        return bookingRepository.countByClassIds(ids, BookingStatus.BOOKED).stream()
                .collect(Collectors.toMap(ClassBookingCount::classId, ClassBookingCount::count));
    }
}