package com.tsystem.model.dto.response;

import com.tsystem.model.enums.ProjectStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private ProjectStatus status;
    private OffsetDateTime createdAt;
}