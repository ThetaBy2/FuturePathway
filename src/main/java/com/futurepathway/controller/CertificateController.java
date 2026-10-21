package com.futurepathway.controller;

import com.futurepathway.dto.ApprovalRequest;
import com.futurepathway.dto.CertificateDto;
import com.futurepathway.dto.CertificateRequest;
import com.futurepathway.security.CurrentUser;
import com.futurepathway.service.CertificateService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Certificates", description = "Student certificates + verification (Modules A/D)")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @Operation(summary = "List my certificates (STUDENT)")
    @GetMapping("/students/me/certificates")
    @PreAuthorize("hasRole('STUDENT')")
    public List<CertificateDto> myCertificates() {
        return certificateService.myCertificates(CurrentUser.email());
    }

    @Operation(summary = "Add a certificate (STUDENT)")
    @PostMapping("/students/me/certificates")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public CertificateDto createCertificate(@Valid @RequestBody CertificateRequest request) {
        return certificateService.createCertificate(CurrentUser.email(), request);
    }

    @Operation(summary = "Update one of my certificates (STUDENT)")
    @PutMapping("/students/me/certificates/{certificateId}")
    @PreAuthorize("hasRole('STUDENT')")
    public CertificateDto updateCertificate(
            @PathVariable Long certificateId, @Valid @RequestBody CertificateRequest request) {
        return certificateService.updateCertificate(CurrentUser.email(), certificateId, request);
    }

    @Operation(summary = "Delete one of my certificates (STUDENT)")
    @DeleteMapping("/students/me/certificates/{certificateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('STUDENT')")
    public void deleteCertificate(@PathVariable Long certificateId) {
        certificateService.deleteCertificate(CurrentUser.email(), certificateId);
    }

    @Operation(summary = "List any student's certificates (all authenticated roles)")
    @GetMapping("/students/{id}/certificates")
    @PreAuthorize("hasAnyRole('STUDENT','COLLEGE','COMPANY','ADMIN')")
    public List<CertificateDto> certificatesOf(@PathVariable Long id) {
        return certificateService.certificatesOf(id);
    }

    @Operation(summary = "Pending certificate queue (COLLEGE/ADMIN/COMPANY)")
    @GetMapping("/certificates/pending")
    @PreAuthorize("hasAnyRole('COLLEGE','ADMIN','COMPANY')")
    public Page<CertificateDto> pendingQueue(@ParameterObject Pageable pageable) {
        return certificateService.pendingQueue(pageable);
    }

    @Operation(summary = "Verify / reject a certificate (COLLEGE/ADMIN/COMPANY)")
    @PutMapping("/certificates/{id}/verify")
    @PreAuthorize("hasAnyRole('COLLEGE','ADMIN','COMPANY')")
    public CertificateDto verifyCertificate(@PathVariable Long id, @Valid @RequestBody ApprovalRequest request) {
        return certificateService.verifyCertificate(id, request.approved(), CurrentUser.email());
    }
}
