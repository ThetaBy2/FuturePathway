package com.futurepathway.controller;

import com.futurepathway.dto.CompanyDto;
import com.futurepathway.dto.CompanyRequest;
import com.futurepathway.security.CurrentUser;
import com.futurepathway.service.CompanyService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Companies", description = "Company profiles (Modules B/C)")
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "List companies (all authenticated roles)")
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','COMPANY','ADMIN')")
    public Page<CompanyDto> listCompanies(@ParameterObject Pageable pageable) {
        return companyService.listCompanies(pageable);
    }

    @Operation(summary = "Get my company profile (COMPANY)")
    @GetMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    public CompanyDto myCompany() {
        return companyService.myCompany(CurrentUser.email());
    }

    @Operation(summary = "Update my company profile (COMPANY)")
    @PutMapping("/me")
    @PreAuthorize("hasRole('COMPANY')")
    public CompanyDto updateMyCompany(@Valid @RequestBody CompanyRequest request) {
        Long id = companyService.myCompany(CurrentUser.email()).id();
        return companyService.updateCompany(id, request, CurrentUser.email());
    }

    @Operation(summary = "Get a company by id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','COMPANY','ADMIN')")
    public CompanyDto getCompany(@PathVariable Long id) {
        return companyService.getCompany(id);
    }

    @Operation(summary = "Update any company (COLLEGE/ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COLLEGE','ADMIN')")
    public CompanyDto updateCompany(@PathVariable Long id, @Valid @RequestBody CompanyRequest request) {
        return companyService.updateCompany(id, request, CurrentUser.email());
    }

    @Operation(summary = "Delete a company and its login (COLLEGE/ADMIN)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('COLLEGE','ADMIN')")
    public void deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
    }
}
