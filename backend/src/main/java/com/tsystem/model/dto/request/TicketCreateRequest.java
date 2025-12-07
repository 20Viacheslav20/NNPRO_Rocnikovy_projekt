package com.tsystem.model.dto.request;


import com.tsystem.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCreateRequest {
    @NotBlank
    @Size(min = 1, max = 160)
    private String name;

    @NotNull
    private TicketType type;

    @NotNull
    private TicketPriority priority;

    @Size(max = 10000)
    private String description;

    private UUID assigneeId;
}