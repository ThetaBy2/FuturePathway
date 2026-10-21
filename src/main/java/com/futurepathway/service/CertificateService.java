package com.futurepathway.service;

import com.futurepathway.dto.CertificateDto;
import com.futurepathway.dto.CertificateRequest;
import com.futurepathway.entity.Certificate;
import com.futurepathway.entity.Student;
import com.futurepathway.enums.VerificationStatus;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.mapper.CertificateMapper;
import com.futurepathway.repository.CertificateRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final StudentService studentService;
    private final CertificateMapper certificateMapper;

    @Transactional(readOnly = true)
    public List<CertificateDto> myCertificates(String email) {
        return certificatesOf(studentService.requireStudentByEmail(email).getId());
    }

    @Transactional(readOnly = true)
    public List<CertificateDto> certificatesOf(Long studentId) {
        studentService.requireStudent(studentId);
        return certificateRepository.findByStudentId(studentId).stream()
                .map(certificateMapper::toDto).toList();
    }

    @Transactional
    public CertificateDto createCertificate(String email, CertificateRequest req) {
        Student student = studentService.requireStudentByEmail(email);
        Certificate certificate = certificateMapper.toEntity(req);
        certificate.setStudent(student);
        certificate.setVerificationStatus(VerificationStatus.PENDING);
        return certificateMapper.toDto(certificateRepository.save(certificate));
    }

    @Transactional
    public CertificateDto updateCertificate(String email, Long certificateId, CertificateRequest req) {
        Certificate certificate = requireMine(email, certificateId);
        certificate.setTitle(req.title().strip());
        certificate.setIssuingOrg(req.issuingOrg());
        certificate.setIssueDate(req.issueDate());
        certificate.setCredentialUrl(req.credentialUrl());
        certificate.setVerificationStatus(VerificationStatus.PENDING); // edits need re-verification
        return certificateMapper.toDto(certificate);
    }

    @Transactional
    public void deleteCertificate(String email, Long certificateId) {
        certificateRepository.delete(requireMine(email, certificateId));
    }

    @Transactional(readOnly = true)
    public Page<CertificateDto> pendingQueue(Pageable pageable) {
        return certificateRepository.findByVerificationStatus(VerificationStatus.PENDING, pageable)
                .map(certificateMapper::toDto);
    }

    @Transactional
    public CertificateDto verifyCertificate(Long certificateId, boolean approved, String verifierEmail) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certificateId));
        certificate.setVerificationStatus(
                approved ? VerificationStatus.VERIFIED : VerificationStatus.REJECTED);
        log.info("Certificate {} {} by {}", certificateId,
                approved ? "VERIFIED" : "REJECTED", verifierEmail);
        return certificateMapper.toDto(certificate);
    }

    private Certificate requireMine(String email, Long certificateId) {
        Student student = studentService.requireStudentByEmail(email);
        return certificateRepository.findByIdAndStudentId(certificateId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certificateId));
    }
}
