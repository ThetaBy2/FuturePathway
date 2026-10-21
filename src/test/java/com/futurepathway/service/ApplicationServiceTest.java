package com.futurepathway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.futurepathway.dto.ApplicationDto;
import com.futurepathway.dto.MatchResultDto;
import com.futurepathway.entity.AppUser;
import com.futurepathway.entity.Application;
import com.futurepathway.entity.Company;
import com.futurepathway.entity.Job;
import com.futurepathway.entity.Student;
import com.futurepathway.enums.Role;
import com.futurepathway.exception.BadRequestException;
import com.futurepathway.exception.ConflictException;
import com.futurepathway.repository.ApplicationRepository;
import com.futurepathway.service.ai.GeminiService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private StudentService studentService;
    @Mock
    private JobService jobService;
    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void apply_computesMatchAndSaves() {
        AppUser user = AppUser.builder().id(1L).email("s@x.io").name("Stu").role(Role.STUDENT).build();
        Student student = Student.builder().id(5L).user(user).rollNo("R1")
                .branch("CSE").batchYear(2026).backlogCount(0).build();
        Company company = Company.builder().id(7L).name("Acme").build();
        Job job = Job.builder().id(9L).company(company).title("Dev")
                .active(true).deadline(LocalDate.now().plusDays(5)).eligibleBranches(List.of()).build();
        when(studentService.requireStudentByEmail("s@x.io")).thenReturn(student);
        when(jobService.requireJob(9L)).thenReturn(job);
        when(applicationRepository.existsByStudentIdAndJobId(5L, 9L)).thenReturn(false);
        MatchResultDto match = new MatchResultDto(5L, 9L, "Dev",
                new BigDecimal("82.50"), "Strong fit", List.of(), "heuristic-fallback");
        when(geminiService.computeProfileMatch(5L, 9L)).thenReturn(match);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application application = inv.getArgument(0);
            application.setId(100L);
            return application;
        });

        ApplicationDto dto = applicationService.apply("s@x.io", 9L);

        assertEquals(100L, dto.id());
        assertEquals(new BigDecimal("82.50"), dto.matchScore());
        assertEquals("Strong fit", dto.matchReasoning());
        assertEquals("Dev", dto.jobTitle());
        assertEquals("Acme", dto.companyName());
    }

    @Test
    void apply_rejectsDuplicatesWithoutCallingAi() {
        Student student = Student.builder().id(5L).build();
        Job job = Job.builder().id(9L).active(true).deadline(LocalDate.now().plusDays(5)).build();
        when(studentService.requireStudentByEmail("s@x.io")).thenReturn(student);
        when(jobService.requireJob(9L)).thenReturn(job);
        when(applicationRepository.existsByStudentIdAndJobId(5L, 9L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> applicationService.apply("s@x.io", 9L));
        verify(geminiService, never()).computeProfileMatch(any(), any());
    }

    @Test
    void apply_rejectsPastDeadline() {
        Student student = Student.builder().id(5L).build();
        Job job = Job.builder().id(9L).active(true).deadline(LocalDate.now().minusDays(1)).build();
        when(studentService.requireStudentByEmail("s@x.io")).thenReturn(student);
        when(jobService.requireJob(9L)).thenReturn(job);

        assertThrows(BadRequestException.class, () -> applicationService.apply("s@x.io", 9L));
    }

    @Test
    void apply_rejectsInactiveJob() {
        Student student = Student.builder().id(5L).build();
        Job job = Job.builder().id(9L).active(false).build();
        when(studentService.requireStudentByEmail("s@x.io")).thenReturn(student);
        when(jobService.requireJob(9L)).thenReturn(job);

        assertThrows(BadRequestException.class, () -> applicationService.apply("s@x.io", 9L));
    }
}
