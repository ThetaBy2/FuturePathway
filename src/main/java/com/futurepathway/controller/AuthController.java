package com.futurepathway.controller;

import com.futurepathway.dto.AuthResponse;
import com.futurepathway.dto.LoginRequest;
import com.futurepathway.dto.RegisterAdminRequest;
import com.futurepathway.dto.RegisterCollegeRequest;
import com.futurepathway.dto.RegisterCompanyRequest;
import com.futurepathway.dto.RegisterStudentRequest;
import com.futurepathway.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Public registration & login; admin-only account provisioning")
@SecurityRequirements // opts out of the global bearerAuth requirement in docs
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a student (public)")
    @PostMapping("/register/student")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        return authService.registerStudent(request);
    }

    @Operation(summary = "Register a company / recruiter (public)")
    @PostMapping("/register/company")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerCompany(@Valid @RequestBody RegisterCompanyRequest request) {
        return authService.registerCompany(request);
    }

    @Operation(summary = "Provision a college admin account (ADMIN only)")
    @PostMapping("/register/college")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AuthResponse registerCollege(@Valid @RequestBody RegisterCollegeRequest request) {
        return authService.registerCollege(request);
    }

    @Operation(summary = "Provision another portal admin (ADMIN only)")
    @PostMapping("/register/admin")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AuthResponse registerAdmin(@Valid @RequestBody RegisterAdminRequest request) {
        return authService.registerAdmin(request);
    }

    @Operation(summary = "Login with email + password, receive a JWT")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
