package com.tsystem.controller;

import com.tsystem.model.dto.request.ProjectCreateRequest;
import com.tsystem.model.dto.request.ProjectUpdateRequest;
import com.tsystem.model.dto.response.ProjectResponse;
import com.tsystem.model.mapper.ProjectMapper;
import com.tsystem.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    // GET /projects
    @GetMapping
    @PreAuthorize("hasAuthority('project:read_all') or hasRole('ADMIN')")
    public List<ProjectResponse> list() {
        return projectService.findAll()
                .stream().map(ProjectMapper::toResponse).toList();
    }

    // POST /projects
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('project:create') or hasRole('ADMIN')")
    public ProjectResponse create(@Valid @RequestBody ProjectCreateRequest req,
                                  @AuthenticationPrincipal UserDetails principal) {
        return ProjectMapper.toResponse(projectService.create(req, principal.getUsername()));
    }

    // GET /projects/{projectId}
    @GetMapping("/{projectId}")
    @PreAuthorize("hasAuthority('project:read_all') or hasRole('ADMIN')")
    public ProjectResponse get(@PathVariable UUID projectId) {
        return ProjectMapper.toResponse(projectService.findById(projectId));
    }

    // PUT /projects/{projectId}
    @PutMapping("/{projectId}")
    @PreAuthorize("hasAuthority('project:update') or hasRole('ADMIN')")
    public ProjectResponse update(@PathVariable UUID projectId,
                                  @Valid @RequestBody ProjectUpdateRequest req) {
        return ProjectMapper.toResponse(projectService.update(projectId, req));
    }

    // DELETE /projects/{projectId}
    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('project:delete') or hasRole('ADMIN')")
    public void delete(@PathVariable UUID projectId,
                       @AuthenticationPrincipal UserDetails principal) {
        projectService.delete(projectId);
    }
}