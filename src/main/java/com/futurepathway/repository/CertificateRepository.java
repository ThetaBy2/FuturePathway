package com.futurepathway.repository;

import com.futurepathway.entity.Certificate;
import com.futurepathway.enums.VerificationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByStudentId(Long studentId);

    Optional<Certificate> findByIdAndStudentId(Long id, Long studentId);

    Page<Certificate> findByVerificationStatus(VerificationStatus status, Pageable pageable);
}
