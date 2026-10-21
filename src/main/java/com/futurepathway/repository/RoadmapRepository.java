package com.futurepathway.repository;

import com.futurepathway.entity.Roadmap;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    List<Roadmap> findByStudentIdOrderByGeneratedAtDesc(Long studentId);
}
