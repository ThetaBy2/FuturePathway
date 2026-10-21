package com.futurepathway.controller;

import com.futurepathway.dto.CollegeDto;
import com.futurepathway.dto.CollegeRequest;
import com.futurepathway.dto.DashboardDto;
import com.futurepathway.dto.StudentDto;
import com.futurepathway.security.CurrentUser;
import com.futurepathway.service.CollegeService;
import com.futurepathway.service.DashboardService;
import com.futurepathway.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "College", description = "TPO workflows: eligibility, students, dashboard (Module C)")
@RestController
@RequestMapping("/api/college")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService collegeService;
    private final StudentService studentService;
    private final DashboardService dashboardService;

    @Operation(summary = "Get my college profile (COLLEGE)")
    @GetMapping("/me")
    @PreAuthorize("hasRole('COLLEGE')")
    public CollegeDto myCollege() {
        return collegeService.myCollege(CurrentUser.email());
    }

    @Operation(summary = "Update my college profile (COLLEGE)")
    @PutMapping("/me")
    @PreAuthorize("hasRole('COLLEGE')")
    public CollegeDto updateMyCollege(@Valid @RequestBody CollegeRequest request) {
        return collegeService.updateMyCollege(CurrentUser.email(), request);
    }

    @Operation(summary = "List all colleges (ADMIN)")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CollegeDto> listColleges(@ParameterObject Pageable pageable) {
        return collegeService.listColleges(pageable);
    }

    @Operation(summary = "List students, optionally by branch (COLLEGE/ADMIN)")
    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('COLLEGE','ADMIN')")
    public Page<StudentDto> listStudents(
            @RequestParam(required = false) String branch,
            @ParameterObject Pageable pageable) {
        return studentService.listStudents(branch, pageable);
    }

    @Operation(summary = "Auto-filter students eligible for a job (managers, COLLEGE, ADMIN)")
    @GetMapping("/jobs/{jobId}/eligible")
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public Page<StudentDto> eligibleStudents(@PathVariable Long jobId, @ParameterObject Pageable pageable) {
        return collegeService.eligibleStudents(jobId, CurrentUser.email(), pageable);
    }

    @Operation(summary = "Placement statistics dashboard (COLLEGE/ADMIN)")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('COLLEGE','ADMIN')")
    public DashboardDto dashboard() {
        return dashboardService.dashboard();
    }
}
