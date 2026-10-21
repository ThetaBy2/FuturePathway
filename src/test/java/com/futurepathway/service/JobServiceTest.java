package com.futurepathway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.futurepathway.dto.JobDto;
import com.futurepathway.dto.JobRequest;
import com.futurepathway.dto.JobSkillRequest;
import com.futurepathway.entity.AppUser;
import com.futurepathway.entity.Company;
import com.futurepathway.entity.Job;
import com.futurepathway.entity.JobSkill;
import com.futurepathway.entity.JobSkillId;
import com.futurepathway.entity.Skill;
import com.futurepathway.enums.Role;
import com.futurepathway.enums.SkillCategory;
import com.futurepathway.repository.JobRepository;
import com.futurepathway.repository.JobSkillRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobSkillRepository jobSkillRepository;
    @Mock
    private CompanyService companyService;
    @Mock
    private StudentService studentService;
    @Mock
    private SkillService skillService;

    @InjectMocks
    private JobService jobService;

    @Test
    void createJob_attachesCompanyAndSkills() {
        Company company = Company.builder().id(7L).name("Acme").build();
        when(companyService.requireCompanyByEmail("hr@acme.io")).thenReturn(company);
        Skill java = Skill.builder().id(1L).name("Java").category(SkillCategory.TECHNICAL).build();
        when(skillService.requireSkill(1L)).thenReturn(java);
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> {
            Job job = inv.getArgument(0);
            job.setId(42L);
            return job;
        });
        Job job = Job.builder().id(42L).company(company).title("Backend Dev").build();
        when(jobSkillRepository.findByJobId(42L)).thenReturn(List.of(
                JobSkill.builder().id(new JobSkillId(42L, 1L)).job(job).skill(java)
                        .requiredProficiency(4).build()));

        JobRequest req = new JobRequest("Backend Dev", "desc", "Engineering",
                new BigDecimal("7.5"), 0, List.of("CSE"), new BigDecimal("12"),
                null, true, List.of(new JobSkillRequest(1L, 4)));
        JobDto dto = jobService.createJob("hr@acme.io", req);

        assertEquals(42L, dto.id());
        assertEquals("Acme", dto.companyName());
        assertEquals(1, dto.skills().size());
        assertEquals("Java", dto.skills().get(0).skillName());
        assertEquals(4, dto.skills().get(0).requiredProficiency());
        verify(jobSkillRepository).deleteByJobId(42L);
    }

    @Test
    void updateJob_deniesOtherCompanies() {
        AppUser owner = AppUser.builder().id(1L).email("owner@x.io").role(Role.COMPANY).build();
        AppUser other = AppUser.builder().id(2L).email("other@y.io").role(Role.COMPANY).build();
        Company company = Company.builder().id(7L).user(owner).name("Acme").build();
        Job job = Job.builder().id(9L).company(company).title("T").eligibleBranches(List.of()).build();
        when(jobRepository.findById(9L)).thenReturn(Optional.of(job));
        when(studentService.requireUser("other@y.io")).thenReturn(other);

        JobRequest req = new JobRequest("T2", null, null, null, null, null, null, null, null, null);
        assertThrows(AccessDeniedException.class, () -> jobService.updateJob(9L, req, "other@y.io"));
    }

    @Test
    void isManager_allowsOwningCompany() {
        AppUser owner = AppUser.builder().id(1L).email("owner@x.io").role(Role.COMPANY).build();
        Company company = Company.builder().id(7L).user(owner).name("Acme").build();
        Job job = Job.builder().id(9L).company(company).title("T").build();
        when(studentService.requireUser("owner@x.io")).thenReturn(owner);

        assertEquals(true, jobService.isManager(job, "owner@x.io"));
    }
}
