package com.futurepathway.service;

import com.futurepathway.dto.SkillAnalysisDto;
import com.futurepathway.entity.SkillAnalysis;
import com.futurepathway.entity.Student;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.repository.SkillAnalysisRepository;
import com.futurepathway.service.ai.GeminiService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SkillAnalysisService {

    private final SkillAnalysisRepository skillAnalysisRepository;
    private final StudentService studentService;
    private final GeminiService geminiService;

    /** "Analyse My Profile" — runs Gemini (or fallback) and stores the result. */
    @Transactional
    public SkillAnalysisDto analyzeMe(String studentEmail) {
        Student student = studentService.requireStudentByEmail(studentEmail);
        GeminiService.AnalysisResult result = geminiService.resumeSkillExtraction(student.getId());
        return toDto(result.analysis(), result.source());
    }

    @Transactional(readOnly = true)
    public SkillAnalysisDto latest(String studentEmail) {
        Student student = studentService.requireStudentByEmail(studentEmail);
        SkillAnalysis analysis = skillAnalysisRepository
                .findTopByStudentIdOrderByGeneratedAtDesc(student.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No analysis yet — call POST /api/ai/analyze-me first"));
        return toDto(analysis, analysis.getSource());
    }

    @Transactional(readOnly = true)
    public List<SkillAnalysisDto> history(String studentEmail) {
        Student student = studentService.requireStudentByEmail(studentEmail);
        return skillAnalysisRepository.findByStudentIdOrderByGeneratedAtDesc(student.getId()).stream()
                .map(a -> toDto(a, a.getSource())).toList();
    }

    private SkillAnalysisDto toDto(SkillAnalysis a, String source) {
        return new SkillAnalysisDto(a.getId(), a.getStudent().getId(),
                a.getStrongSkills(), a.getWeakSkills(), a.getSummaryText(), a.getGeneratedAt(), source);
    }
}
