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

import com.tsystem.exception.ForbiddenException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projects;
    private final UserRepository users;

    // TODO нужно переделать
    private Project mustOwnProject(UUID projectId, String username) {
        Project p = projects.findById(projectId).orElseThrow(NotFoundException::new);
        if (!p.getUser().getUsername().equals(username)) throw new ForbiddenException();
        return p;
    }

    @Transactional
    public Project create(ProjectCreateRequest req, String username) {
        User owner = users.findByEmail(username)
                .orElseThrow(NotFoundException::new);

        Project p = Project.builder()
                .name(req.getName())
                .description(req.getDescription())
                .user(owner)
                .build();
        return projects.save(p);
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projects.findAll();
    }

    @Transactional(readOnly = true)
    public Project findById(UUID uuid) {
        return projects.findById(uuid).orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Project update(UUID projectId, ProjectUpdateRequest req, String username) {
        Project p = mustOwnProject(projectId, username);
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setStatus(req.getStatus());
        return projects.save(p);
    }

    @Transactional
    public void delete(UUID projectId, String username) {
        Project p = mustOwnProject(projectId, username);
        projects.delete(p);
    }
}