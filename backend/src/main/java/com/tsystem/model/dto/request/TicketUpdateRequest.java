package com.tsystem.model.dto.request;


import com.tsystem.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketUpdateRequest {
    @NotBlank
    @Size(min = 1, max = 160)
    private String name;

    @NotNull
    private TicketType type;

    @NotNull
    private TicketPriority priority;

    @NotNull
    private TicketState state;

    @Size(max = 10000)
    private String description;
}
