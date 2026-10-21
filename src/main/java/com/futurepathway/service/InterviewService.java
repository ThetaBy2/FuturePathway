package com.futurepathway.service;

import com.futurepathway.dto.InterviewProcessDto;
import com.futurepathway.dto.InterviewProcessRequest;
import com.futurepathway.dto.InterviewSlotDto;
import com.futurepathway.dto.InterviewSlotRequest;
import com.futurepathway.entity.InterviewProcess;
import com.futurepathway.entity.InterviewSlot;
import com.futurepathway.entity.Job;
import com.futurepathway.entity.Student;
import com.futurepathway.enums.SlotStatus;
import com.futurepathway.exception.ConflictException;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.repository.InterviewProcessRepository;
import com.futurepathway.repository.InterviewSlotRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewProcessRepository processRepository;
    private final InterviewSlotRepository slotRepository;
    private final JobService jobService;
    private final StudentService studentService;

    // ── interview process (rounds + criteria per job) ─────────────────────

    @Transactional(readOnly = true)
    public InterviewProcessDto getProcess(Long jobId) {
        return toDto(requireProcessByJob(jobId));
    }

    @Transactional
    public InterviewProcessDto upsertProcess(Long jobId, InterviewProcessRequest req, String requesterEmail) {
        Job job = jobService.requireJob(jobId);
        assertManages(job, requesterEmail);
        InterviewProcess process = processRepository.findByJobId(jobId)
                .orElseGet(() -> InterviewProcess.builder().job(job).build());
        process.setRounds(req.rounds() == null ? List.of() : req.rounds());
        process.setSelectionCriteria(req.selectionCriteria() == null ? Map.of() : req.selectionCriteria());
        return toDto(processRepository.save(process));
    }

    // ── slots ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<InterviewSlotDto> listSlots(Long processId, String requesterEmail) {
        InterviewProcess process = requireProcess(processId);
        assertManages(process.getJob(), requesterEmail);
        return slotRepository.findByInterviewProcessId(processId).stream()
                .map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<InterviewSlotDto> mySlots(String studentEmail) {
        Student student = studentService.requireStudentByEmail(studentEmail);
        return slotRepository.findByStudentIdOrderByScheduledAtAsc(student.getId()).stream()
                .map(this::toDto).toList();
    }

    @Transactional
    public InterviewSlotDto createSlot(Long processId, InterviewSlotRequest req, String requesterEmail) {
        InterviewProcess process = requireProcess(processId);
        assertManages(process.getJob(), requesterEmail);
        Student student = studentService.requireStudent(req.studentId());
        if (slotRepository.existsByInterviewProcessIdAndStudentIdAndRoundName(
                processId, student.getId(), req.roundName().strip())) {
            throw new ConflictException("Slot already exists for this student and round");
        }
        InterviewSlot slot = InterviewSlot.builder()
                .interviewProcess(process)
                .student(student)
                .roundName(req.roundName().strip())
                .scheduledAt(req.scheduledAt())
                .venueOrLink(req.venueOrLink())
                .status(req.status() == null ? SlotStatus.SCHEDULED : req.status())
                .build();
        log.info("Scheduled {} slot for student {} (process {})", slot.getRoundName(),
                student.getId(), processId);
        return toDto(slotRepository.save(slot));
    }

    @Transactional
    public InterviewSlotDto updateSlot(Long slotId, InterviewSlotRequest req, String requesterEmail) {
        InterviewSlot slot = requireSlot(slotId);
        assertManages(slot.getInterviewProcess().getJob(), requesterEmail);
        slot.setRoundName(req.roundName().strip());
        slot.setScheduledAt(req.scheduledAt());
        slot.setVenueOrLink(req.venueOrLink());
        if (req.status() != null) {
            slot.setStatus(req.status());
        }
        return toDto(slot);
    }

    @Transactional
    public void deleteSlot(Long slotId, String requesterEmail) {
        InterviewSlot slot = requireSlot(slotId);
        assertManages(slot.getInterviewProcess().getJob(), requesterEmail);
        slotRepository.delete(slot);
    }

    private void assertManages(Job job, String requesterEmail) {
        if (!jobService.isManager(job, requesterEmail)) {
            throw new AccessDeniedException("Not allowed to manage interviews for this job");
        }
    }

    private InterviewProcess requireProcess(Long id) {
        return processRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview process not found: " + id));
    }

    private InterviewProcess requireProcessByJob(Long jobId) {
        jobService.requireJob(jobId);
        return processRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("No interview process defined for job: " + jobId));
    }

    private InterviewSlot requireSlot(Long id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview slot not found: " + id));
    }

    @Transactional(readOnly = true)
    public InterviewProcessDto toDto(InterviewProcess p) {
        return new InterviewProcessDto(p.getId(), p.getJob().getId(), p.getJob().getTitle(),
                p.getRounds(), p.getSelectionCriteria());
    }

    @Transactional(readOnly = true)
    public InterviewSlotDto toDto(InterviewSlot s) {
        return new InterviewSlotDto(
                s.getId(), s.getInterviewProcess().getId(),
                s.getInterviewProcess().getJob().getId(), s.getInterviewProcess().getJob().getTitle(),
                s.getStudent().getId(), s.getStudent().getUser().getName(),
                s.getRoundName(), s.getScheduledAt(), s.getVenueOrLink(), s.getStatus());
    }
}
