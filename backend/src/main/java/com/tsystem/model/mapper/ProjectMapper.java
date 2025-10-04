package com.tsystem.model.mapper;


import com.tsystem.model.Project;
import com.tsystem.model.dto.response.ProjectResponse;

public final class ProjectMapper {
    private ProjectMapper(){}

    public static ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}