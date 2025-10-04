package com.tsystem.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCreateRequest {
    @NotBlank
    @Size(min = 1, max = 120)
    private String name;

    @Size(max = 5000)
    private String description;
}