package com.futurepathway.service;

import com.futurepathway.dto.RoadmapDto;
import com.futurepathway.entity.Roadmap;
import com.futurepathway.entity.Student;
import com.futurepathway.repository.RoadmapRepository;
import com.futurepathway.service.ai.GeminiService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final StudentService studentService;
    private final GeminiService geminiService;

    @Transactional
    public RoadmapDto generate(String studentEmail, String targetRole) {
        Student student = studentService.requireStudentByEmail(studentEmail);
        GeminiService.RoadmapResult result = geminiService.generateRoadmap(student.getId(), targetRole.strip());
        return toDto(result.roadmap(), result.source());
    }

    @Transactional(readOnly = true)
    public List<RoadmapDto> history(String studentEmail) {
        Student student = studentService.requireStudentByEmail(studentEmail);
        return roadmapRepository.findByStudentIdOrderByGeneratedAtDesc(student.getId()).stream()
                .map(r -> toDto(r, r.getSource())).toList();
    }

    private RoadmapDto toDto(Roadmap r, String source) {
        return new RoadmapDto(r.getId(), r.getStudent().getId(), r.getTargetRole(),
                r.getMilestones(), r.getGeneratedAt(), source);
    }
}
