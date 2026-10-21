package com.futurepathway.service;

import com.futurepathway.dto.CollegeDto;
import com.futurepathway.dto.CollegeRequest;
import com.futurepathway.dto.StudentDto;
import com.futurepathway.entity.AppUser;
import com.futurepathway.entity.College;
import com.futurepathway.entity.InterviewProcess;
import com.futurepathway.entity.Job;
import com.futurepathway.entity.Student;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.mapper.CollegeMapper;
import com.futurepathway.repository.CollegeRepository;
import com.futurepathway.repository.InterviewProcessRepository;
import com.futurepathway.repository.StudentRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollegeService {

    private final CollegeRepository collegeRepository;
    private final StudentRepository studentRepository;
    private final InterviewProcessRepository interviewProcessRepository;
    private final StudentService studentService;
    private final JobService jobService;
    private final CollegeMapper collegeMapper;

    @Transactional(readOnly = true)
    public CollegeDto myCollege(String email) {
        return collegeMapper.toDto(requireCollegeByEmail(email));
    }

    @Transactional
    public CollegeDto updateMyCollege(String email, CollegeRequest req) {
        College college = requireCollegeByEmail(email);
        college.setName(req.name().strip());
        college.setAddress(req.address());
        college.setTpoContactEmail(req.tpoContactEmail());
        return collegeMapper.toDto(college);
    }

    @Transactional(readOnly = true)
    public Page<CollegeDto> listColleges(Pageable pageable) {
        return collegeRepository.findAll(pageable).map(collegeMapper::toDto);
    }

    /**
     * Auto-filters students eligible for a job: merges the job's own criteria
     * (min CGPA / branches / backlog limit) with the interview process'
     * {@code selection_criteria} JSONB when present (process wins).
     */
    @Transactional(readOnly = true)
    public Page<StudentDto> eligibleStudents(Long jobId, String requesterEmail, Pageable pageable) {
        Job job = jobService.requireJob(jobId);
        if (!jobService.isManager(job, requesterEmail)) {
            throw new AccessDeniedException("Not allowed to view eligible students for this job");
        }
        List<String> branches = job.getEligibleBranches();
        BigDecimal minCgpa = job.getMinCgpa();
        Integer maxBacklogs = job.getMaxBacklogs();

        Optional<InterviewProcess> process = interviewProcessRepository.findByJobId(jobId);
        if (process.isPresent() && process.get().getSelectionCriteria() != null) {
            Map<String, Object> criteria = process.get().getSelectionCriteria();
            if (criteria.get("branches") instanceof List<?> list && !list.isEmpty()) {
                branches = list.stream().map(String::valueOf).toList();
            }
            if (criteria.get("min_cgpa") instanceof Number number) {
                minCgpa = new BigDecimal(number.toString());
            }
            if (criteria.get("max_backlogs") instanceof Number number) {
                maxBacklogs = number.intValue();
            }
        }
        return studentRepository.findAll(eligibleSpec(branches, minCgpa, maxBacklogs), pageable)
                .map(studentService::toDto);
    }

    private Specification<Student> eligibleSpec(
            List<String> branches, BigDecimal minCgpa, Integer maxBacklogs) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (branches != null && !branches.isEmpty()) {
                var in = cb.in(cb.lower(root.get("branch")));
                branches.stream()
                        .filter(b -> b != null && !b.isBlank())
                        .forEach(b -> in.value(b.toLowerCase(Locale.ROOT)));
                predicates.add(in);
            }
            if (minCgpa != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("cgpa"), minCgpa));
            }
            if (maxBacklogs != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("backlogCount"), maxBacklogs));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public College requireCollegeByEmail(String email) {
        AppUser user = studentService.requireUser(email);
        return collegeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No college profile for user: " + email));
    }
}
