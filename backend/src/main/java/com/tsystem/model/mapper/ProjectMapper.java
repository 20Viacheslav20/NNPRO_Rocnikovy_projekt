package com.tsystem.model.mapper;


import com.tsystem.model.Project;
import com.tsystem.model.dto.response.ProjectResponse;
import com.tsystem.model.dto.response.UserShortResponse;
import com.tsystem.model.user.User;

public final class ProjectMapper {
    private ProjectMapper(){}

    public static ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .owner(toOwnerResponse(p.getUser()))
                .build();
    }

    private static UserShortResponse toOwnerResponse(User u) {
        return UserShortResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .name(u.getName())
                .surname(u.getSurname())
                .build();
    }

}