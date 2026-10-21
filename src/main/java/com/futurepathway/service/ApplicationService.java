package com.futurepathway.service;

import com.futurepathway.dto.ApplicationDto;
import com.futurepathway.dto.MatchResultDto;
import com.futurepathway.entity.Application;
import com.futurepathway.entity.Job;
import com.futurepathway.entity.Student;
import com.futurepathway.enums.ApplicationStatus;
import com.futurepathway.exception.BadRequestException;
import com.futurepathway.exception.ConflictException;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.repository.ApplicationRepository;
import com.futurepathway.service.ai.GeminiService;
import java.time.LocalDate;
import java.util.List;
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
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentService studentService;
    private final JobService jobService;
    private final GeminiService geminiService;

    /** Student applies: guards + Gemini profile→job match stored on the row. */
    @Transactional
    public ApplicationDto apply(String studentEmail, Long jobId) {
        Student student = studentService.requireStudentByEmail(studentEmail);
        Job job = jobService.requireJob(jobId);
        if (!job.isActive()) {
            throw new BadRequestException("This job is no longer accepting applications");
        }
        if (job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now())) {
            throw new BadRequestException("The application deadline has passed");
        }
        if (applicationRepository.existsByStudentIdAndJobId(student.getId(), jobId)) {
            throw new ConflictException("You have already applied to this job");
        }
        MatchResultDto match = geminiService.computeProfileMatch(student.getId(), jobId);
        Application saved = applicationRepository.save(Application.builder()
                .student(student)
                .job(job)
                .status(ApplicationStatus.APPLIED)
                .matchScore(match.matchScore())
                .matchReasoning(match.reasoning())
                .build());
        log.info("Student {} applied to job {} (match {})", student.getId(), jobId, match.matchScore());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> myApplications(String studentEmail) {
        Student student = studentService.requireStudentByEmail(studentEmail);
        return applicationRepository.findByStudentIdOrderByAppliedAtDesc(student.getId()).stream()
                .map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<ApplicationDto> applicationsForJob(Long jobId, String requesterEmail, Pageable pageable) {
        Job job = jobService.requireJob(jobId);
        if (!jobService.isManager(job, requesterEmail)) {
            throw new AccessDeniedException("Not allowed to view applications for this job");
        }
        return applicationRepository.findByJobId(jobId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ApplicationDto getApplication(Long id, String requesterEmail) {
        Application application = requireApplication(id);
        boolean mine = application.getStudent().getUser().getEmail().equalsIgnoreCase(requesterEmail);
        if (!mine && !jobService.isManager(application.getJob(), requesterEmail)) {
            throw new AccessDeniedException("Not allowed to view this application");
        }
        return toDto(application);
    }

    @Transactional
    public ApplicationDto updateStatus(Long id, ApplicationStatus status, String requesterEmail) {
        Application application = requireApplication(id);
        if (!jobService.isManager(application.getJob(), requesterEmail)) {
            throw new AccessDeniedException("Not allowed to update this application");
        }
        application.setStatus(status);
        log.info("Application {} → {} by {}", id, status, requesterEmail);
        return toDto(application);
    }

    private Application requireApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
    }

    @Transactional(readOnly = true)
    public ApplicationDto toDto(Application a) {
        return new ApplicationDto(
                a.getId(),
                a.getStudent().getId(), a.getStudent().getUser().getName(),
                a.getStudent().getRollNo(), a.getStudent().getBranch(), a.getStudent().getCgpa(),
                a.getJob().getId(), a.getJob().getTitle(), a.getJob().getCompany().getName(),
                a.getStatus(), a.getMatchScore(), a.getMatchReasoning(), a.getAppliedAt());
    }
}
