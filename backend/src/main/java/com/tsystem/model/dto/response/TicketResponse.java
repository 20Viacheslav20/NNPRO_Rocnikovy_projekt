package com.tsystem.model.dto.response;


import com.tsystem.model.enums.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private UUID id;
    private String name;
    private String description;
    private TicketType type;
    private TicketPriority priority;
    private TicketState state;
    private OffsetDateTime createdAt;
    private UserShortResponse owner;
    private UserShortResponse assignee;
    private UUID projectId;
}