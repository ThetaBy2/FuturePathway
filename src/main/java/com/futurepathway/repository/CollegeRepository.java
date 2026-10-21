package com.futurepathway.repository;

import com.futurepathway.entity.College;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {
    Optional<College> findByUserId(Long userId);
}
