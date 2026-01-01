package com.tsystem.model.dto.response;

import com.tsystem.model.enums.TicketPriority;
import com.tsystem.model.enums.TicketState;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class TicketHistoryResponse {

    private UUID id;
    private UUID authorId;
    private String action;
    private String field;
    private String oldValue;
    private String newValue;
    private OffsetDateTime createdAt;
}
