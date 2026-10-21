package com.futurepathway.controller;

import com.futurepathway.dto.StudentSkillDto;
import com.futurepathway.dto.StudentSkillRequest;
import com.futurepathway.security.CurrentUser;
import com.futurepathway.service.SkillService;
import com.futurepathway.service.StudentService;
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

@Tag(name = "Student skills", description = "Skills on a student's profile (Module A)")
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentSkillController {

    private final SkillService skillService;
    private final StudentService studentService;

    @Operation(summary = "List skills on my profile (STUDENT)")
    @GetMapping("/me/skills")
    @PreAuthorize("hasRole('STUDENT')")
    public List<StudentSkillDto> mySkills() {
        Long studentId = studentService.requireStudentByEmail(CurrentUser.email()).getId();
        return skillService.listStudentSkills(studentId);
    }

    @Operation(summary = "Add a skill to my profile (STUDENT)")
    @PostMapping("/me/skills")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public StudentSkillDto addSkill(@Valid @RequestBody StudentSkillRequest request) {
        return skillService.addStudentSkill(CurrentUser.email(), request);
    }

    @Operation(summary = "Update a skill entry on my profile (STUDENT)")
    @PutMapping("/me/skills/{entryId}")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentSkillDto updateSkill(
            @PathVariable Long entryId, @Valid @RequestBody StudentSkillRequest request) {
        return skillService.updateStudentSkill(CurrentUser.email(), entryId, request);
    }

    @Operation(summary = "Remove a skill entry from my profile (STUDENT)")
    @DeleteMapping("/me/skills/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('STUDENT')")
    public void removeSkill(@PathVariable Long entryId) {
        skillService.removeStudentSkill(CurrentUser.email(), entryId);
    }

    @Operation(summary = "List any student's skills (all authenticated roles)")
    @GetMapping("/{id}/skills")
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','COMPANY','ADMIN')")
    public List<StudentSkillDto> skillsOf(@PathVariable Long id) {
        return skillService.listStudentSkills(id);
    }
}
