package com.futurepathway.controller;

import com.futurepathway.dto.JobDto;
import com.futurepathway.dto.JobRequest;
import com.futurepathway.dto.JobSkillRequest;
import com.futurepathway.security.CurrentUser;
import com.futurepathway.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Jobs", description = "Job postings & required skills (Module B)")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @Operation(summary = "Search jobs by domain / status / package (all authenticated roles)")
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','COMPANY','ADMIN')")
    public Page<JobDto> searchJobs(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) BigDecimal minPackage,
            @ParameterObject Pageable pageable) {
        return jobService.searchJobs(domain, active, minPackage, pageable);
    }

    @Operation(summary = "List jobs posted by my company (COMPANY)")
    @GetMapping("/my")
    @PreAuthorize("hasRole('COMPANY')")
    public List<JobDto> myJobs() {
        return jobService.myJobs(CurrentUser.email());
    }

    @Operation(summary = "Get a job by id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','COMPANY','ADMIN')")
    public JobDto getJob(@PathVariable Long id) {
        return jobService.getJob(id);
    }

    @Operation(summary = "Post a job (COMPANY)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COMPANY')")
    public JobDto createJob(@Valid @RequestBody JobRequest request) {
        return jobService.createJob(CurrentUser.email(), request);
    }

    @Operation(summary = "Update a job (owning company, COLLEGE or ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public JobDto updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return jobService.updateJob(id, request, CurrentUser.email());
    }

    @Operation(summary = "Delete a job (owning company, COLLEGE or ADMIN)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public void deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id, CurrentUser.email());
    }

    @Operation(summary = "Replace a job's required skill set (owning company, COLLEGE or ADMIN)")
    @PutMapping("/{id}/skills")
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public JobDto setSkills(@PathVariable Long id, @Valid @RequestBody List<JobSkillRequest> skills) {
        return jobService.setSkills(id, skills, CurrentUser.email());
    }
}
