package com.tsystem.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCommentResponse {
    private UUID id;
    private UUID ticketId;
    private UUID commentAuthorId;
    private String text;
}
