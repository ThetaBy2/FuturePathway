package com.futurepathway.controller;

import com.futurepathway.dto.ApplicationDto;
import com.futurepathway.dto.UpdateApplicationStatusRequest;
import com.futurepathway.security.CurrentUser;
import com.futurepathway.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Applications", description = "Job applications + match scores (Module B)")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "Apply to a job — computes & stores the Gemini match score (STUDENT)")
    @PostMapping("/jobs/{jobId}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public ApplicationDto apply(@PathVariable Long jobId) {
        return applicationService.apply(CurrentUser.email(), jobId);
    }

    @Operation(summary = "List my applications (STUDENT)")
    @GetMapping("/students/me/applications")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ApplicationDto> myApplications() {
        return applicationService.myApplications(CurrentUser.email());
    }

    @Operation(summary = "List applications for a job (owning company, COLLEGE or ADMIN)")
    @GetMapping("/jobs/{jobId}/applications")
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public Page<ApplicationDto> applicationsForJob(
            @PathVariable Long jobId, @ParameterObject Pageable pageable) {
        return applicationService.applicationsForJob(jobId, CurrentUser.email(), pageable);
    }

    @Operation(summary = "Get one application (candidate, owning company, COLLEGE or ADMIN)")
    @GetMapping("/applications/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','COMPANY','COLLEGE','ADMIN')")
    public ApplicationDto getApplication(@PathVariable Long id) {
        return applicationService.getApplication(id, CurrentUser.email());
    }

    @Operation(summary = "Move an application through the pipeline (owning company, COLLEGE or ADMIN)")
    @PutMapping("/applications/{id}/status")
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public ApplicationDto updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return applicationService.updateStatus(id, request.status(), CurrentUser.email());
    }
}
