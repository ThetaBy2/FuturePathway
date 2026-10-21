package com.futurepathway.repository;

import com.futurepathway.entity.SkillAnalysis;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillAnalysisRepository extends JpaRepository<SkillAnalysis, Long> {
    Optional<SkillAnalysis> findTopByStudentIdOrderByGeneratedAtDesc(Long studentId);

    List<SkillAnalysis> findByStudentIdOrderByGeneratedAtDesc(Long studentId);
}
