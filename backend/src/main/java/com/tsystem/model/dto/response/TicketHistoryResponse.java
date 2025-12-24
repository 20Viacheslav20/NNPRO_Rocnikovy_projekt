package com.tsystem.model.dto.response;

import com.tsystem.model.enums.TicketPriority;
import com.tsystem.model.enums.TicketState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketHistoryResponse {

    private UUID id;
    private UUID ticketId;
    private UUID changedById;

    private TicketState oldState;
    private TicketState newState;

    private TicketPriority oldPriority;
    private TicketPriority newPriority;

    private UUID oldAssigneeId;
    private UUID newAssigneeId;

    private OffsetDateTime changedAt;
}
