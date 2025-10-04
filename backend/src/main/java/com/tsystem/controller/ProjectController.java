package com.tsystem.controller;
import com.tsystem.model.Project;

import com.tsystem.model.dto.request.ProjectCreateRequest;
import com.tsystem.model.dto.request.ProjectUpdateRequest;
import com.tsystem.model.dto.response.ProjectResponse;
import com.tsystem.model.mapper.ProjectMapper;
import com.tsystem.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projects;

    // GET /projects
    @GetMapping
    public List<ProjectResponse> list(@AuthenticationPrincipal UserDetails principal) {
        return projects.listMine(principal.getUsername())
                .stream().map(ProjectMapper::toResponse).toList();
    }

    // POST /projects
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectCreateRequest req,
                                  @AuthenticationPrincipal UserDetails principal) {
        return ProjectMapper.toResponse(projects.create(req, principal.getUsername()));
    }

    // GET /projects/{projectId}
    @GetMapping("/{projectId}")
    public ProjectResponse get(@PathVariable UUID projectId,
                               @AuthenticationPrincipal UserDetails principal) {
        return ProjectMapper.toResponse(projects.getMine(projectId, principal.getUsername()));
    }

    // PUT /projects/{projectId}
    @PutMapping("/{projectId}")
    public ProjectResponse update(@PathVariable UUID projectId,
                                  @Valid @RequestBody ProjectUpdateRequest req,
                                  @AuthenticationPrincipal UserDetails principal) {
        return ProjectMapper.toResponse(projects.update(projectId, req, principal.getUsername()));
    }

    // DELETE /projects/{projectId}
    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID projectId,
                       @AuthenticationPrincipal UserDetails principal) {
        projects.delete(projectId, principal.getUsername());
    }
}