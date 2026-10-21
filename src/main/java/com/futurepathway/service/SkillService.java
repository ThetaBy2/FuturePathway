package com.futurepathway.service;

import com.futurepathway.dto.SkillDto;
import com.futurepathway.dto.SkillRequest;
import com.futurepathway.dto.StudentSkillDto;
import com.futurepathway.dto.StudentSkillRequest;
import com.futurepathway.entity.Skill;
import com.futurepathway.entity.Student;
import com.futurepathway.entity.StudentSkill;
import com.futurepathway.enums.SkillSource;
import com.futurepathway.exception.BadRequestException;
import com.futurepathway.exception.ConflictException;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.mapper.SkillMapper;
import com.futurepathway.mapper.StudentSkillMapper;
import com.futurepathway.repository.SkillRepository;
import com.futurepathway.repository.StudentSkillRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final StudentService studentService;
    private final SkillMapper skillMapper;
    private final StudentSkillMapper studentSkillMapper;

    // ── admin-managed skill catalog ──────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<SkillDto> listSkills(Pageable pageable) {
        return skillRepository.findAll(pageable).map(skillMapper::toDto);
    }

    @Transactional(readOnly = true)
    public SkillDto getSkill(Long id) {
        return skillMapper.toDto(requireSkill(id));
    }

    @Transactional
    public SkillDto createSkill(SkillRequest req) {
        if (skillRepository.existsByNameIgnoreCase(req.name().strip())) {
            throw new ConflictException("Skill already exists: " + req.name());
        }
        Skill saved = skillRepository.save(skillMapper.toEntity(req));
        log.info("Created skill {}", saved.getName());
        return skillMapper.toDto(saved);
    }

    @Transactional
    public SkillDto updateSkill(Long id, SkillRequest req) {
        Skill skill = requireSkill(id);
        skillRepository.findByNameIgnoreCase(req.name().strip())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("Skill already exists: " + req.name());
                });
        skill.setName(req.name().strip());
        skill.setCategory(req.category());
        skill.setDescription(req.description());
        return skillMapper.toDto(skill);
    }

    @Transactional
    public void deleteSkill(Long id) {
        skillRepository.delete(requireSkill(id));
    }

    // ── student's own skills ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StudentSkillDto> listStudentSkills(Long studentId) {
        studentService.requireStudent(studentId);
        return studentSkillRepository.findByStudentId(studentId).stream()
                .map(studentSkillMapper::toDto).toList();
    }

    @Transactional
    public StudentSkillDto addStudentSkill(String requesterEmail, StudentSkillRequest req) {
        Student student = studentService.requireStudentByEmail(requesterEmail);
        Skill skill = requireSkill(req.skillId());
        if (studentSkillRepository.findByStudentIdAndSkillId(student.getId(), skill.getId()).isPresent()) {
            throw new ConflictException("Skill already on profile; update its proficiency instead");
        }
        StudentSkill saved = studentSkillRepository.save(StudentSkill.builder()
                .student(student)
                .skill(skill)
                .proficiencyLevel(req.proficiencyLevel())
                .source(SkillSource.SELF_DECLARED)
                .build());
        return studentSkillMapper.toDto(saved);
    }

    @Transactional
    public StudentSkillDto updateStudentSkill(String requesterEmail, Long entryId, StudentSkillRequest req) {
        Student student = studentService.requireStudentByEmail(requesterEmail);
        StudentSkill entry = studentSkillRepository.findByIdAndStudentId(entryId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student skill not found: " + entryId));
        if (!entry.getSkill().getId().equals(req.skillId())) {
            throw new BadRequestException("Skill cannot be changed; delete this entry and re-add");
        }
        entry.setProficiencyLevel(req.proficiencyLevel());
        if (entry.getSource() == SkillSource.VERIFIED) {
            entry.setSource(SkillSource.SELF_DECLARED); // re-rating needs re-verification
            entry.setVerifiedBy(null);
            entry.setVerifiedAt(null);
        }
        return studentSkillMapper.toDto(entry);
    }

    @Transactional
    public void removeStudentSkill(String requesterEmail, Long entryId) {
        Student student = studentService.requireStudentByEmail(requesterEmail);
        StudentSkill entry = studentSkillRepository.findByIdAndStudentId(entryId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student skill not found: " + entryId));
        studentSkillRepository.delete(entry);
    }

    // ── verification workflow ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<StudentSkillDto> verificationQueue(Pageable pageable) {
        return studentSkillRepository.findBySourceNot(SkillSource.VERIFIED, pageable)
                .map(studentSkillMapper::toDto);
    }

    @Transactional
    public StudentSkillDto verifyStudentSkill(Long entryId, boolean approved, String verifierEmail) {
        StudentSkill entry = studentSkillRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Student skill not found: " + entryId));
        if (approved) {
            entry.setSource(SkillSource.VERIFIED);
            entry.setVerifiedBy(verifierEmail);
            entry.setVerifiedAt(Instant.now());
        } else {
            entry.setSource(SkillSource.SELF_DECLARED);
            entry.setVerifiedBy(null);
            entry.setVerifiedAt(null);
        }
        log.info("Skill entry {} {} by {}", entryId, approved ? "VERIFIED" : "REJECTED", verifierEmail);
        return studentSkillMapper.toDto(entry);
    }

    public Skill requireSkill(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));
    }
}
