package com.futurepathway.repository;

import com.futurepathway.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStudentId(Long studentId);

    Optional<Project> findByIdAndStudentId(Long id, Long studentId);
}
