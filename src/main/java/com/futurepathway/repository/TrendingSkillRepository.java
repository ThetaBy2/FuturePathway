package com.futurepathway.repository;

import com.futurepathway.entity.TrendingSkill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrendingSkillRepository extends JpaRepository<TrendingSkill, Long> {
    Optional<TrendingSkill> findBySkillId(Long skillId);

    List<TrendingSkill> findAllByOrderByDemandScoreDesc();
}
