package com.futurepathway.repository;

import com.futurepathway.entity.Job;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByCompanyId(Long companyId);

    long countByCompanyId(Long companyId);

    long countByActiveTrue();

    @Query("select j from Job j where "
            + "(:domain is null or lower(j.jobDomain) = lower(:domain)) "
            + "and (:active is null or j.active = :active) "
            + "and (:minPackage is null or j.packageLpa >= :minPackage)")
    Page<Job> search(@Param("domain") String domain,
                     @Param("active") Boolean active,
                     @Param("minPackage") BigDecimal minPackage,
                     Pageable pageable);
}
