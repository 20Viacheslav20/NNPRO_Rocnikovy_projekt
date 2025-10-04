package com.tsystem.model.dto.request;

import com.tsystem.model.enums.ProjectStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectUpdateRequest {
    @NotBlank
    @Size(min = 1, max = 120)
    private String name;

    @Size(max = 5000)
    private String description;

    @NotNull
    private ProjectStatus status;
}