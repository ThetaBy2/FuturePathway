package com.futurepathway.service;

import com.futurepathway.dto.StudentDto;
import com.futurepathway.dto.UpdateStudentRequest;
import com.futurepathway.entity.AppUser;
import com.futurepathway.entity.Student;
import com.futurepathway.enums.Role;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.repository.CollegeRepository;
import com.futurepathway.repository.StudentRepository;
import com.futurepathway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;

    @Transactional(readOnly = true)
    public Page<StudentDto> listStudents(String branch, Pageable pageable) {
        Page<Student> page = (branch == null || branch.isBlank())
                ? studentRepository.findAll(pageable)
                : studentRepository.findByBranchIgnoreCase(branch.strip(), pageable);
        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public StudentDto getStudent(Long id) {
        return toDto(requireStudent(id));
    }

    @Transactional(readOnly = true)
    public StudentDto getMyProfile(String email) {
        return toDto(requireStudentByEmail(email));
    }

    @Transactional
    public StudentDto updateStudent(Long id, UpdateStudentRequest req, String requesterEmail) {
        Student student = requireStudent(id);
        AppUser requester = requireUser(requesterEmail);
        if (requester.getRole() == Role.STUDENT && !student.getUser().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Students can only edit their own profile");
        }
        if (req.name() != null && !req.name().isBlank()) {
            student.getUser().setName(req.name().strip());
        }
        if (req.branch() != null && !req.branch().isBlank()) {
            student.setBranch(req.branch().strip());
        }
        if (req.batchYear() != null) {
            student.setBatchYear(req.batchYear());
        }
        if (req.cgpa() != null) {
            student.setCgpa(req.cgpa());
        }
        if (req.backlogCount() != null) {
            student.setBacklogCount(req.backlogCount());
        }
        if (req.cvUrl() != null) {
            student.setCvUrl(req.cvUrl());
        }
        if (req.profilePhotoUrl() != null) {
            student.setProfilePhotoUrl(req.profilePhotoUrl());
        }
        if (req.resumeText() != null) {
            student.setResumeText(req.resumeText());
        }
        if (req.collegeId() != null) {
            student.setCollege(collegeRepository.findById(req.collegeId())
                    .orElseThrow(() -> new ResourceNotFoundException("College not found: " + req.collegeId())));
        }
        log.info("Updated student {} by {}", id, requesterEmail);
        return toDto(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = requireStudent(id);
        userRepository.delete(student.getUser()); // FK cascade removes the student row
        log.info("Deleted student {}", id);
    }

    public Student requireStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    public Student requireStudentByEmail(String email) {
        AppUser user = requireUser(email);
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No student profile for user: " + email));
    }

    public AppUser requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional(readOnly = true)
    public StudentDto toDto(Student s) {
        AppUser u = s.getUser();
        return new StudentDto(
                s.getId(), u.getId(), u.getName(), u.getEmail(),
                s.getRollNo(), s.getBranch(), s.getBatchYear(), s.getCgpa(), s.getBacklogCount(),
                s.getCvUrl(), s.getProfilePhotoUrl(),
                s.getCollege() == null ? null : s.getCollege().getId(),
                s.getCollege() == null ? null : s.getCollege().getName(),
                s.getCreatedAt(), s.getUpdatedAt());
    }
}
