package com.futurepathway.repository;

import com.futurepathway.entity.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByRollNo(String rollNo);

    boolean existsByRollNo(String rollNo);

    Page<Student> findByBranchIgnoreCase(String branch, Pageable pageable);

    @Query("select s.branch, count(s) from Student s group by s.branch")
    List<Object[]> countGroupedByBranch();
}
