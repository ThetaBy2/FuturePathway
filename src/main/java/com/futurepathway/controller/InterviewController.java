package com.futurepathway.controller;

import com.futurepathway.dto.InterviewProcessDto;
import com.futurepathway.dto.InterviewProcessRequest;
import com.futurepathway.dto.InterviewSlotDto;
import com.futurepathway.dto.InterviewSlotRequest;
import com.futurepathway.security.CurrentUser;
import com.futurepathway.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Interviews", description = "Rounds, criteria & slots (Module C)")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @Operation(summary = "Get the interview process (rounds + criteria) for a job")
    @GetMapping("/jobs/{jobId}/process")
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public InterviewProcessDto getProcess(@PathVariable Long jobId) {
        return interviewService.getProcess(jobId);
    }

    @Operation(summary = "Create or replace the interview process for a job")
    @PutMapping("/jobs/{jobId}/process")
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public InterviewProcessDto upsertProcess(
            @PathVariable Long jobId, @Valid @RequestBody InterviewProcessRequest request) {
        return interviewService.upsertProcess(jobId, request, CurrentUser.email());
    }

    @Operation(summary = "List slots of an interview process")
    @GetMapping("/processes/{processId}/slots")
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public List<InterviewSlotDto> listSlots(@PathVariable Long processId) {
        return interviewService.listSlots(processId, CurrentUser.email());
    }

    @Operation(summary = "Schedule a slot (assign a student to a round)")
    @PostMapping("/processes/{processId}/slots")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public InterviewSlotDto createSlot(
            @PathVariable Long processId, @Valid @RequestBody InterviewSlotRequest request) {
        return interviewService.createSlot(processId, request, CurrentUser.email());
    }

    @Operation(summary = "Reschedule / update a slot")
    @PutMapping("/slots/{slotId}")
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public InterviewSlotDto updateSlot(
            @PathVariable Long slotId, @Valid @RequestBody InterviewSlotRequest request) {
        return interviewService.updateSlot(slotId, request, CurrentUser.email());
    }

    @Operation(summary = "Cancel (delete) a slot")
    @DeleteMapping("/slots/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('COMPANY','COLLEGE','ADMIN')")
    public void deleteSlot(@PathVariable Long slotId) {
        interviewService.deleteSlot(slotId, CurrentUser.email());
    }

    @Operation(summary = "List my upcoming interview slots (STUDENT)")
    @GetMapping("/students/me/slots")
    @PreAuthorize("hasRole('STUDENT')")
    public List<InterviewSlotDto> mySlots() {
        return interviewService.mySlots(CurrentUser.email());
    }
}
