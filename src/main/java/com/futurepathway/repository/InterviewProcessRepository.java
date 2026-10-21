package com.futurepathway.repository;

import com.futurepathway.entity.InterviewProcess;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewProcessRepository extends JpaRepository<InterviewProcess, Long> {
    Optional<InterviewProcess> findByJobId(Long jobId);
}
