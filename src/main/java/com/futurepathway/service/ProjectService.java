package com.futurepathway.service;

import com.futurepathway.dto.ProjectDto;
import com.futurepathway.dto.ProjectRequest;
import com.futurepathway.entity.Project;
import com.futurepathway.entity.Student;
import com.futurepathway.exception.ResourceNotFoundException;
import com.futurepathway.mapper.ProjectMapper;
import com.futurepathway.repository.ProjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final StudentService studentService;
    private final ProjectMapper projectMapper;

    @Transactional(readOnly = true)
    public List<ProjectDto> myProjects(String email) {
        return projectsOf(studentService.requireStudentByEmail(email).getId());
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> projectsOf(Long studentId) {
        studentService.requireStudent(studentId);
        return projectRepository.findByStudentId(studentId).stream()
                .map(projectMapper::toDto).toList();
    }

    @Transactional
    public ProjectDto createProject(String email, ProjectRequest req) {
        Student student = studentService.requireStudentByEmail(email);
        Project project = projectMapper.toEntity(req);
        project.setStudent(student);
        return projectMapper.toDto(projectRepository.save(project));
    }

    @Transactional
    public ProjectDto updateProject(String email, Long projectId, ProjectRequest req) {
        Project project = requireMine(email, projectId);
        project.setTitle(req.title().strip());
        project.setDescription(req.description());
        project.setTechStack(req.techStack());
        project.setRepoLink(req.repoLink());
        project.setDuration(req.duration());
        return projectMapper.toDto(project);
    }

    @Transactional
    public void deleteProject(String email, Long projectId) {
        projectRepository.delete(requireMine(email, projectId));
    }

    private Project requireMine(String email, Long projectId) {
        Student student = studentService.requireStudentByEmail(email);
        return projectRepository.findByIdAndStudentId(projectId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }
}
