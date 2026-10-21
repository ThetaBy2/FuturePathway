package com.futurepathway.repository;

import com.futurepathway.entity.StudentSkill;
import com.futurepathway.enums.SkillSource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentSkillRepository extends JpaRepository<StudentSkill, Long> {
    List<StudentSkill> findByStudentId(Long studentId);

    Optional<StudentSkill> findByIdAndStudentId(Long id, Long studentId);

    Optional<StudentSkill> findByStudentIdAndSkillId(Long studentId, Long skillId);

    Page<StudentSkill> findBySourceNot(SkillSource source, Pageable pageable);
}
