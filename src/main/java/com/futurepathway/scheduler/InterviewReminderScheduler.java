package com.futurepathway.scheduler;

import com.futurepathway.entity.InterviewSlot;
import com.futurepathway.enums.SlotStatus;
import com.futurepathway.repository.InterviewSlotRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hourly reminder pass for interview slots in the next 24h.
 * Reminder delivery (email/push) would plug in here; currently logs each reminder.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewReminderScheduler {

    private final InterviewSlotRepository slotRepository;

    @Scheduled(cron = "${app.scheduler.reminder-cron:0 0 * * * *}")
    @Transactional(readOnly = true)
    public void sendReminders() {
        Instant now = Instant.now();
        List<InterviewSlot> upcoming =
                slotRepository.findByStatusAndScheduledAtBetween(SlotStatus.SCHEDULED, now, now.plus(24, ChronoUnit.HOURS));
        for (InterviewSlot slot : upcoming) {
            log.info("REMINDER: {} round for {} ({}) at {} — {}",
                    slot.getRoundName(),
                    slot.getStudent().getUser().getName(),
                    slot.getStudent().getUser().getEmail(),
                    slot.getScheduledAt(),
                    slot.getVenueOrLink());
        }
        if (!upcoming.isEmpty()) {
            log.info("Sent {} interview reminders", upcoming.size());
        }
    }
}
