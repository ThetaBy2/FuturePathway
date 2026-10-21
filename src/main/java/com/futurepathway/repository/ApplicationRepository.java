package com.futurepathway.repository;

import com.futurepathway.entity.Application;
import com.futurepathway.enums.ApplicationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudentIdOrderByAppliedAtDesc(Long studentId);

    Page<Application> findByJobId(Long jobId, Pageable pageable);

    Optional<Application> findByStudentIdAndJobId(Long studentId, Long jobId);

    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);

    long countByStatus(ApplicationStatus status);

    @Query("select a.status, count(a) from Application a group by a.status")
    List<Object[]> countGroupedByStatus();

    @Query("select j.company.name, count(a) from Application a join a.job j group by j.company.name")
    List<Object[]> countGroupedByCompany();

    @Query("select j.company.name, count(a) from Application a join a.job j "
            + "where a.status = com.futurepathway.enums.ApplicationStatus.SELECTED group by j.company.name")
    List<Object[]> countSelectedGroupedByCompany();

    @Query("select s.branch, a.status, count(a) from Application a join a.student s group by s.branch, a.status")
    List<Object[]> countGroupedByBranchAndStatus();
}
