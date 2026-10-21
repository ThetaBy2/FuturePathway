package com.futurepathway.controller;

import com.futurepathway.dto.StudentDto;
import com.futurepathway.dto.UpdateStudentRequest;
import com.futurepathway.security.CurrentUser;
import com.futurepathway.service.StudentService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Students", description = "Student profiles (Module A)")
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "List students, optionally by branch (COLLEGE/ADMIN)")
    @GetMapping
    @PreAuthorize("hasAnyRole('COLLEGE','ADMIN')")
    public Page<StudentDto> listStudents(
            @RequestParam(required = false) String branch,
            @ParameterObject Pageable pageable) {
        return studentService.listStudents(branch, pageable);
    }

    @Operation(summary = "Get my own profile (STUDENT)")
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentDto myProfile() {
        return studentService.getMyProfile(CurrentUser.email());
    }

    @Operation(summary = "Update my own profile (STUDENT)")
    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentDto updateMyProfile(@Valid @RequestBody UpdateStudentRequest request) {
        Long id = studentService.getMyProfile(CurrentUser.email()).id();
        return studentService.updateStudent(id, request, CurrentUser.email());
    }

    @Operation(summary = "Get a student by id (all authenticated roles)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','COMPANY','ADMIN')")
    public StudentDto getStudent(@PathVariable Long id) {
        return studentService.getStudent(id);
    }

    @Operation(summary = "Update a student (owner, COLLEGE or ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','ADMIN')")
    public StudentDto updateStudent(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        return studentService.updateStudent(id, request, CurrentUser.email());
    }

    @Operation(summary = "Delete a student (COLLEGE/ADMIN)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('COLLEGE','ADMIN')")
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }
}
