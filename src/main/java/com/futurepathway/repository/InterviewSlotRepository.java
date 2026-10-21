package com.futurepathway.repository;

import com.futurepathway.entity.InterviewSlot;
import com.futurepathway.enums.SlotStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSlotRepository extends JpaRepository<InterviewSlot, Long> {
    List<InterviewSlot> findByStudentIdOrderByScheduledAtAsc(Long studentId);

    List<InterviewSlot> findByInterviewProcessId(Long processId);

    List<InterviewSlot> findByStatusAndScheduledAtBetween(SlotStatus status, Instant from, Instant to);

    boolean existsByInterviewProcessIdAndStudentIdAndRoundName(Long processId, Long studentId, String roundName);
}
