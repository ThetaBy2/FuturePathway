package com.futurepathway.service;

import com.futurepathway.dto.JobDto;
import com.futurepathway.dto.JobRequest;
import com.futurepathway.dto.JobSkillDto;
import com.futurepathway.dto.JobSkillRequest;
import com.futurepathway.entity.AppUser;
import com.futurepathway.entity.Company;
import com.futurepathway.entity.Job;
import com.futurepathway.entity.JobSkill;
import com.futurepathway.entity.JobSkillId;
import com.futurepathway.entity.Skill;
import com.futurepathway.enums.Role;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.repository.JobRepository;
import com.futurepathway.repository.JobSkillRepository;
import java.math.BigDecimal;
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
public class JobService {

    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final CompanyService companyService;
    private final StudentService studentService;
    private final SkillService skillService;

    @Transactional(readOnly = true)
    public Page<JobDto> searchJobs(String domain, Boolean active, BigDecimal minPackage, Pageable pageable) {
        String d = (domain == null || domain.isBlank()) ? null : domain.strip();
        return jobRepository.search(d, active, minPackage, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public JobDto getJob(Long id) {
        return toDto(requireJob(id));
    }

    @Transactional(readOnly = true)
    public List<JobDto> myJobs(String email) {
        return jobRepository.findByCompanyId(companyService.requireCompanyByEmail(email).getId()).stream()
                .map(this::toDto).toList();
    }

    @Transactional
    public JobDto createJob(String email, JobRequest req) {
        Company company = companyService.requireCompanyByEmail(email);
        Job job = Job.builder()
                .company(company)
                .title(req.title().strip())
                .description(req.description())
                .jobDomain(req.jobDomain())
                .minCgpa(req.minCgpa())
                .maxBacklogs(req.maxBacklogs())
                .eligibleBranches(req.eligibleBranches() == null ? List.of() : req.eligibleBranches())
                .packageLpa(req.packageLpa())
                .deadline(req.deadline())
                .active(req.active() == null || req.active())
                .build();
        job = jobRepository.save(job);
        replaceSkills(job, req.skills());
        log.info("Company {} posted job {} ({})", company.getName(), job.getId(), job.getTitle());
        return toDto(job);
    }

    @Transactional
    public JobDto updateJob(Long id, JobRequest req, String requesterEmail) {
        Job job = requireJob(id);
        assertCanManage(job, requesterEmail);
        job.setTitle(req.title().strip());
        job.setDescription(req.description());
        job.setJobDomain(req.jobDomain());
        job.setMinCgpa(req.minCgpa());
        job.setMaxBacklogs(req.maxBacklogs());
        job.setEligibleBranches(req.eligibleBranches() == null ? List.of() : req.eligibleBranches());
        job.setPackageLpa(req.packageLpa());
        job.setDeadline(req.deadline());
        if (req.active() != null) {
            job.setActive(req.active());
        }
        if (req.skills() != null) {
            replaceSkills(job, req.skills());
        }
        return toDto(job);
    }

    @Transactional
    public void deleteJob(Long id, String requesterEmail) {
        Job job = requireJob(id);
        assertCanManage(job, requesterEmail);
        jobRepository.delete(job);
        log.info("Deleted job {}", id);
    }

    @Transactional
    public JobDto setSkills(Long id, List<JobSkillRequest> skills, String requesterEmail) {
        Job job = requireJob(id);
        assertCanManage(job, requesterEmail);
        replaceSkills(job, skills);
        return toDto(job);
    }

    public Job requireJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
    }

    private void replaceSkills(Job job, List<JobSkillRequest> skills) {
        jobSkillRepository.deleteByJobId(job.getId());
        if (skills == null || skills.isEmpty()) {
            return;
        }
        for (JobSkillRequest item : skills) {
            Skill skill = skillService.requireSkill(item.skillId());
            jobSkillRepository.save(JobSkill.builder()
                    .id(new JobSkillId(job.getId(), skill.getId()))
                    .job(job)
                    .skill(skill)
                    .requiredProficiency(item.requiredProficiency() == null ? 3 : item.requiredProficiency())
                    .build());
        }
    }

    private void assertCanManage(Job job, String requesterEmail) {
        if (!isManager(job, requesterEmail)) {
            throw new AccessDeniedException("Not allowed to manage this job");
        }
    }

    @Transactional(readOnly = true)
    public JobDto toDto(Job job) {
        List<JobSkillDto> skills = jobSkillRepository.findByJobId(job.getId()).stream()
                .map(js -> new JobSkillDto(js.getSkill().getId(), js.getSkill().getName(),
                        js.getRequiredProficiency()))
                .toList();
        return new JobDto(
                job.getId(), job.getCompany().getId(), job.getCompany().getName(),
                job.getTitle(), job.getDescription(), job.getJobDomain(),
                job.getMinCgpa(), job.getMaxBacklogs(), job.getEligibleBranches(),
                job.getPackageLpa(), job.getDeadline(), job.isActive(), skills, job.getCreatedAt());
    }

    /**
     * Shared ownership check: the job's company owner, or COLLEGE / ADMIN staff.
     * Used by ApplicationService and InterviewService too.
     */
    public boolean isManager(Job job, String email) {
        AppUser requester;
        try {
            requester = studentService.requireUser(email);
        } catch (ResourceNotFoundException ex) {
            return false;
        }
        boolean owner = job.getCompany().getUser() != null
                && job.getCompany().getUser().getId().equals(requester.getId());
        return owner || requester.getRole() == Role.COLLEGE || requester.getRole() == Role.ADMIN;
    }
}
