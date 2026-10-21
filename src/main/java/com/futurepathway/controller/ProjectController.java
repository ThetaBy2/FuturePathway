package com.futurepathway.controller;

import com.futurepathway.dto.ProjectDto;
import com.futurepathway.dto.ProjectRequest;
import com.futurepathway.security.CurrentUser;
import com.futurepathway.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Projects", description = "Student projects (Module A)")
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "List my projects (STUDENT)")
    @GetMapping("/me/projects")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ProjectDto> myProjects() {
        return projectService.myProjects(CurrentUser.email());
    }

    @Operation(summary = "Add a project (STUDENT)")
    @PostMapping("/me/projects")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public ProjectDto createProject(@Valid @RequestBody ProjectRequest request) {
        return projectService.createProject(CurrentUser.email(), request);
    }

    @Operation(summary = "Update one of my projects (STUDENT)")
    @PutMapping("/me/projects/{projectId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ProjectDto updateProject(@PathVariable Long projectId, @Valid @RequestBody ProjectRequest request) {
        return projectService.updateProject(CurrentUser.email(), projectId, request);
    }

    @Operation(summary = "Delete one of my projects (STUDENT)")
    @DeleteMapping("/me/projects/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('STUDENT')")
    public void deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(CurrentUser.email(), projectId);
    }

    @Operation(summary = "List any student's projects (all authenticated roles)")
    @GetMapping("/{id}/projects")
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','COMPANY','ADMIN')")
    public List<ProjectDto> projectsOf(@PathVariable Long id) {
        return projectService.projectsOf(id);
    }
}
