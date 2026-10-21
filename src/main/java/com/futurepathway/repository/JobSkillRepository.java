package com.futurepathway.repository;

import com.futurepathway.entity.JobSkill;
import com.futurepathway.entity.JobSkillId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobSkillRepository extends JpaRepository<JobSkill, JobSkillId> {
    @Query("select js from JobSkill js join fetch js.skill where js.job.id = :jobId")
    List<JobSkill> findByJobId(@Param("jobId") Long jobId);

    @Modifying
    @Query("delete from JobSkill js where js.job.id = :jobId")
    void deleteByJobId(@Param("jobId") Long jobId);

    /** Demand signal for trending skills: active postings per skill. */
    @Query("select js.skill, count(js) from JobSkill js join js.job j "
            + "where j.active = true group by js.skill")
    List<Object[]> countActiveDemandBySkill();
}
