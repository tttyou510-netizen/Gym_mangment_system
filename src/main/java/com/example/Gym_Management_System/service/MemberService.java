package com.example.Gym_Management_System.service;

import com.example.Gym_Management_System.common.ResourceNotFoundException;
import com.example.Gym_Management_System.common.exception.DuplicateResourceException;
import com.example.Gym_Management_System.dto.CreateMemberRequest;
import com.example.Gym_Management_System.dto.MemberResponse;
import com.example.Gym_Management_System.dto.PageResponse;
import com.example.Gym_Management_System.dto.UpdateMemberRequest;
import com.example.Gym_Management_System.emuns.Role;
import com.example.Gym_Management_System.model.Member;
import com.example.Gym_Management_System.model.User;
import com.example.Gym_Management_System.repository.MemberRepository;
import com.example.Gym_Management_System.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MemberResponse create(CreateMemberRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already in use: " + email);
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(Role.MEMBER);
        user.setEnabled(true);
        userRepository.save(user);

        Member member = new Member();
        member.setUser(user);
        member.setBirthDate(request.birthDate());
        member.setGender(request.gender());
        member.setEmergencyContact(request.emergencyContact());

        return MemberResponse.from(memberRepository.save(member));
    }

    public MemberResponse getById(Long id) {
        return MemberResponse.from(findOrThrow(id));
    }

    public PageResponse<MemberResponse> search(String name, Pageable pageable) {
        Page<Member> page = (name == null || name.isBlank())
                ? memberRepository.findAll(pageable)
                : memberRepository.searchByName(name.trim(), pageable);

        return PageResponse.from(page.map(MemberResponse::from));
    }

    @Transactional
    public MemberResponse update(Long id, UpdateMemberRequest request) {
        Member member = findOrThrow(id);

        member.getUser().setFullName(request.fullName().trim());
        member.getUser().setPhone(request.phone());
        member.setBirthDate(request.birthDate());
        member.setGender(request.gender());
        member.setEmergencyContact(request.emergencyContact());

        return MemberResponse.from(member);
    }

    @Transactional
    public void deactivate(Long id) {
        findOrThrow(id).getUser().setEnabled(false);
    }

    private Member findOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));
    }
}