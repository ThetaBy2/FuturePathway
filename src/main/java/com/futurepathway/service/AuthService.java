package com.futurepathway.service;

import com.futurepathway.dto.AuthResponse;
import com.futurepathway.dto.LoginRequest;
import com.futurepathway.dto.RegisterAdminRequest;
import com.futurepathway.dto.RegisterCollegeRequest;
import com.futurepathway.dto.RegisterCompanyRequest;
import com.futurepathway.dto.RegisterStudentRequest;
import com.futurepathway.dto.UserDto;
import com.futurepathway.entity.AppUser;
import com.futurepathway.entity.College;
import com.futurepathway.entity.Company;
import com.futurepathway.entity.Student;
import com.futurepathway.enums.Role;
import com.futurepathway.exception.ConflictException;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.repository.CollegeRepository;
import com.futurepathway.repository.CompanyRepository;
import com.futurepathway.repository.StudentRepository;
import com.futurepathway.repository.UserRepository;
import com.futurepathway.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final CollegeRepository collegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registerStudent(RegisterStudentRequest req) {
        ensureEmailFree(req.email());
        if (studentRepository.existsByRollNo(req.rollNo().strip())) {
            throw new ConflictException("Roll number already registered: " + req.rollNo());
        }
        AppUser user = saveUser(req.name(), req.email(), req.password(), Role.STUDENT);
        College college = req.collegeId() == null ? null : collegeRepository.findById(req.collegeId())
                .orElseThrow(() -> new ResourceNotFoundException("College not found: " + req.collegeId()));
        Student student = Student.builder()
                .user(user)
                .rollNo(req.rollNo().strip())
                .branch(req.branch().strip())
                .batchYear(req.batchYear())
                .cgpa(req.cgpa())
                .backlogCount(req.backlogCount() == null ? 0 : req.backlogCount())
                .college(college)
                .cvUrl(req.cvUrl())
                .resumeText(req.resumeText())
                .build();
        student = studentRepository.save(student);
        log.info("Registered student {} ({})", user.getEmail(), student.getRollNo());
        return token(user, student.getId());
    }

    @Transactional
    public AuthResponse registerCompany(RegisterCompanyRequest req) {
        ensureEmailFree(req.email());
        AppUser user = saveUser(req.contactName(), req.email(), req.password(), Role.COMPANY);
        Company company = Company.builder()
                .user(user)
                .name(req.companyName().strip())
                .industryDomain(req.industryDomain())
                .website(req.website())
                .hrContactEmail(req.hrContactEmail() == null ? req.email() : req.hrContactEmail())
                .build();
        company = companyRepository.save(company);
        log.info("Registered company {} ({})", company.getName(), user.getEmail());
        return token(user, company.getId());
    }

    @Transactional
    public AuthResponse registerCollege(RegisterCollegeRequest req) {
        ensureEmailFree(req.email());
        AppUser user = saveUser(req.contactName(), req.email(), req.password(), Role.COLLEGE);
        College college = College.builder()
                .user(user)
                .name(req.collegeName().strip())
                .address(req.address())
                .tpoContactEmail(req.tpoContactEmail() == null ? req.email() : req.tpoContactEmail())
                .build();
        college = collegeRepository.save(college);
        log.info("Registered college {} ({})", college.getName(), user.getEmail());
        return token(user, college.getId());
    }

    @Transactional
    public AuthResponse registerAdmin(RegisterAdminRequest req) {
        ensureEmailFree(req.email());
        AppUser user = saveUser(req.name(), req.email(), req.password(), Role.ADMIN);
        log.info("Registered admin {}", user.getEmail());
        return token(user, null);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        AppUser user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.email()));
        return token(user, profileIdFor(user));
    }

    @Transactional(readOnly = true)
    public Page<UserDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(u -> new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole(),
                        u.isEnabled(), u.getCreatedAt()));
    }

    private AppUser saveUser(String name, String email, String rawPassword, Role role) {
        return userRepository.save(AppUser.builder()
                .name(name.strip())
                .email(email.strip().toLowerCase())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .enabled(true)
                .build());
    }

    private void ensureEmailFree(String email) {
        if (userRepository.existsByEmail(email.strip().toLowerCase())) {
            throw new ConflictException("Email already registered: " + email);
        }
    }

    private AuthResponse token(AppUser user, Long profileId) {
        return new AuthResponse(jwtService.generateToken(user), user.getId(),
                user.getName(), user.getEmail(), user.getRole(), profileId);
    }

    private Long profileIdFor(AppUser user) {
        return switch (user.getRole()) {
            case STUDENT -> studentRepository.findByUserId(user.getId()).map(Student::getId).orElse(null);
            case COMPANY -> companyRepository.findByUserId(user.getId()).map(Company::getId).orElse(null);
            case COLLEGE -> collegeRepository.findByUserId(user.getId()).map(College::getId).orElse(null);
            case ADMIN -> null;
        };
    }
}
