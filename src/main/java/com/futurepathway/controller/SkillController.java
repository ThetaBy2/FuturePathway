package com.futurepathway.controller;

import com.futurepathway.dto.ApprovalRequest;
import com.futurepathway.dto.SkillDto;
import com.futurepathway.dto.SkillRequest;
import com.futurepathway.dto.StudentSkillDto;
import com.futurepathway.security.CurrentUser;
import com.futurepathway.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Skill catalog", description = "Admin-managed master skill list + verification (Module D)")
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @Operation(summary = "List the skill catalog (all authenticated roles)")
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','COMPANY','ADMIN')")
    public Page<SkillDto> listSkills(@ParameterObject Pageable pageable) {
        return skillService.listSkills(pageable);
    }

    @Operation(summary = "Get a skill by id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','COMPANY','ADMIN')")
    public SkillDto getSkill(@PathVariable Long id) {
        return skillService.getSkill(id);
    }

    @Operation(summary = "Add a skill to the catalog (ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SkillDto createSkill(@Valid @RequestBody SkillRequest request) {
        return skillService.createSkill(request);
    }

    @Operation(summary = "Update a catalog skill (ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SkillDto updateSkill(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        return skillService.updateSkill(id, request);
    }

    @Operation(summary = "Delete a catalog skill (ADMIN)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
    }

    @Operation(summary = "Unverified student-skill queue (COLLEGE/ADMIN/COMPANY)")
    @GetMapping("/verification-queue")
    @PreAuthorize("hasAnyRole('COLLEGE','ADMIN','COMPANY')")
    public Page<StudentSkillDto> verificationQueue(@ParameterObject Pageable pageable) {
        return skillService.verificationQueue(pageable);
    }

    @Operation(summary = "Verify / reject a student skill (COLLEGE/ADMIN/COMPANY)")
    @PutMapping("/student-skills/{entryId}/verify")
    @PreAuthorize("hasAnyRole('COLLEGE','ADMIN','COMPANY')")
    public StudentSkillDto verifyStudentSkill(
            @PathVariable Long entryId, @Valid @RequestBody ApprovalRequest request) {
        return skillService.verifyStudentSkill(entryId, request.approved(), CurrentUser.email());
    }
}
