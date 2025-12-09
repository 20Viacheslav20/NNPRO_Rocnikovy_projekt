package com.tsystem.service;

import com.tsystem.exception.NotFoundException;
import com.tsystem.model.Project;
import com.tsystem.model.user.User;

import com.tsystem.model.dto.request.ProjectCreateRequest;
import com.tsystem.model.dto.request.ProjectUpdateRequest;
import com.tsystem.repository.ProjectRepository;
import com.tsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public Project create(ProjectCreateRequest req, String username) {
        User owner = userRepository.findByEmail(username)
                .orElseThrow(NotFoundException::new);

        Project p = Project.builder()
                .name(req.getName())
                .description(req.getDescription())
                .user(owner)
                .build();
        return projectRepository.save(p);
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Project findById(UUID uuid) {
        return projectRepository.findById(uuid).orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Project update(UUID projectId, ProjectUpdateRequest req) {

        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setStatus(req.getStatus());
        return projectRepository.save(p);
    }

    @Transactional
    public void delete(UUID projectId) {
        projectRepository.deleteById(projectId);
    }
}